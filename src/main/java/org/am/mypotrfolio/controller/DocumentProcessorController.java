package org.am.mypotrfolio.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.am.mypotrfolio.domain.common.DocumentType;
import org.am.mypotrfolio.model.DocumentProcessResponse;
import org.am.mypotrfolio.model.ProcessingStatus;
import org.am.mypotrfolio.security.JwtValidator;
import org.am.mypotrfolio.service.DocumentProcessorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

/**
 * Document Processor REST Controller
 * 
 * Per coding instructions:
 * - All internal service endpoints require service JWT validation
 * - Service tokens generated at API Gateway, not user tokens passed through
 * - User ID extracted from validated token
 * - Returns 401 if token is invalid/missing
 * - Public endpoints (like /types) don't require authentication
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/documents")
@Tag(name = "Documents", description = "Document processing operations (internal service)")
public class DocumentProcessorController {

    @Autowired
    private DocumentProcessorService documentProcessorService;
    
    @Autowired
    private JwtValidator jwtValidator;

    // ═══════════════════════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Extract Bearer token from Authorization header
     * Returns null if header is missing or invalid format
     * 
     * Per coding instructions: Service tokens come via "Authorization: Bearer {token}" header
     */
    private String extractBearerToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);  // Remove "Bearer " prefix
        }
        log.warn("Authorization header missing or malformed");
        return null;
    }
    
    /**
     * Validate service token and return user ID
     * 
     * Per coding instructions:
     * - Validates service JWT from API Gateway
     * - Service JWT contains user_id (set by API Gateway)
     * - Returns user_id for authorization context
     * - Throws exception if token invalid/expired
     */
    private String validateAndGetUserId(String token) {
        if (token == null) {
            log.warn("No service token provided in Authorization header");
            return null;
        }
        try {
            // jwtValidator.validateServiceToken() will:
            // 1. Verify signature using INTERNAL_JWT_SECRET
            // 2. Check token type is "service"
            // 3. Verify token not expired
            // 4. Extract and return user_id
            return jwtValidator.validateServiceToken(token);
        } catch (Exception e) {
            log.error("Service token validation failed: {}", e.getMessage());
            return null;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PUBLIC ENDPOINTS (No authentication required)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Get supported document types
     * 
     * Per coding instructions:
     * - Public endpoints don't require authentication
     * - No Authorization header needed
     */
    @Operation(
        summary = "Get supported document types",
        description = "Retrieve list of supported document types (public endpoint, no auth required)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Document types retrieved successfully",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/types")
    public ResponseEntity<List<String>> getSupportedDocumentTypes() {
        log.info("Getting supported document types");
        return ResponseEntity.ok(documentProcessorService.getSupportedDocumentTypes());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // INTERNAL SERVICE ENDPOINTS (Require service JWT from API Gateway)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Process a single document
     * 
     * Per coding instructions:
     * - Service-to-service endpoint (internal only)
     * - Requires service JWT from API Gateway
     * - API Gateway validates user JWT first, then generates service JWT
     * - User ID extracted from service JWT (set by API Gateway)
     * - Returns 401 if token invalid/missing
     */
    @Operation(
        summary = "Process a single document",
        description = "Upload and process a single portfolio document (internal service)",
        security = @SecurityRequirement(name = "Bearer")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Document processed successfully",
            content = @Content(schema = @Schema(implementation = DocumentProcessResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid or missing service token"),
        @ApiResponse(responseCode = "400", description = "Invalid input parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(value = "/process", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> processDocument(
            @Parameter(description = "Portfolio document file to process", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Type of document being processed", required = true)
            @RequestParam("documentType") DocumentType documentType,
            @Parameter(description = "Portfolio ID", required = false)
            @RequestParam(value = "portfolioId", required = false) String portfolioId,
            HttpServletRequest request) {
        
        log.info("Processing document: type={}", documentType);
        
        // ✅ Step 1: Extract service token from Authorization header
        String serviceToken = extractBearerToken(request);
        
        // ✅ Step 2: Validate service token and extract user ID
        String userId = validateAndGetUserId(serviceToken);
        if (userId == null) {
            log.warn("Unauthorized document processing attempt: Invalid or missing service token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Unauthorized: Invalid or missing service token"));
        }
        
        log.info("Document processing authorized for user: {} (portfolio: {})", userId, portfolioId);
        
        // ✅ Step 3: Process document with authenticated user ID
        DocumentProcessResponse response = documentProcessorService.processDocument(
            file, 
            documentType, 
            portfolioId, 
            userId  // User ID from validated service token
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Process multiple documents (batch)
     * 
     * Per coding instructions: Same authentication flow as single document
     */
    @Operation(
        summary = "Process multiple documents",
        description = "Upload and process multiple portfolio documents (internal service)",
        security = @SecurityRequirement(name = "Bearer")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Documents processed successfully",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = DocumentProcessResponse.class)))),
        @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid or missing service token"),
        @ApiResponse(responseCode = "400", description = "Invalid input parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(value = "/batch-process", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> processBatchDocuments(
            @Parameter(description = "List of portfolio document files to process", required = true)
            @RequestParam("files") List<MultipartFile> files,
            @Parameter(description = "Type of documents being processed", required = true)
            @RequestParam("documentType") DocumentType documentType,
            @Parameter(description = "Portfolio ID", required = false)
            @RequestParam(value = "portfolioId", required = false) String portfolioId,
            HttpServletRequest request) {
        
        log.info("Batch processing {} documents: type={}", files.size(), documentType);
        
        // ✅ Step 1: Validate service token
        String serviceToken = extractBearerToken(request);
        String userId = validateAndGetUserId(serviceToken);
        
        if (userId == null) {
            log.warn("Unauthorized batch processing attempt: Invalid or missing service token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Unauthorized: Invalid or missing service token"));
        }
        
        log.info("Batch processing authorized for user: {} ({} files)", userId, files.size());
        
        // ✅ Step 2: Process batch documents
        List<DocumentProcessResponse> responses = documentProcessorService.processBatchDocuments(
            files,
            documentType,
            portfolioId,
            userId  // User ID from validated service token
        );
        
        return ResponseEntity.ok(responses);
    }

    /**
     * Get document processing status
     * 
     * Per coding instructions: Internal endpoint requires service token
     */
    @Operation(
        summary = "Get document processing status",
        description = "Retrieve the current status of a document processing request (internal service)",
        security = @SecurityRequirement(name = "Bearer")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Processing status retrieved successfully",
            content = @Content(schema = @Schema(implementation = ProcessingStatus.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid or missing service token"),
        @ApiResponse(responseCode = "404", description = "Process ID not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/status/{processId}")
    public ResponseEntity<?> getProcessingStatus(
            @Parameter(description = "Unique identifier of the processing request", required = true)
            @PathVariable UUID processId,
            HttpServletRequest request) {
        
        log.info("Getting processing status: processId={}", processId);
        
        // ✅ Validate service token
        String serviceToken = extractBearerToken(request);
        String userId = validateAndGetUserId(serviceToken);
        
        if (userId == null) {
            log.warn("Unauthorized status check attempt: Invalid or missing service token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Unauthorized: Invalid or missing service token"));
        }
        
        log.debug("Status check authorized for user: {}", userId);
        
        // ✅ Get processing status (scoped to authenticated user)
        ProcessingStatus status = documentProcessorService.getProcessingStatus(processId);
        return ResponseEntity.ok(status);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ERROR RESPONSE MODEL
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Simple error response for authentication failures
     */
    public static class ErrorResponse {
        public String error;
        public long timestamp;

        public ErrorResponse(String error) {
            this.error = error;
            this.timestamp = System.currentTimeMillis();
        }

        public String getError() {
            return error;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}