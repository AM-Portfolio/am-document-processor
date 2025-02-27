package org.am.mypotrfolio.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.am.mypotrfolio.domain.common.DocumentType;
import org.am.mypotrfolio.model.DocumentProcessResponse;
import org.am.mypotrfolio.model.ProcessingStatus;
import org.am.mypotrfolio.service.DocumentProcessorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
@Tag(name = "Documents", description = "Document processing operations")
public class DocumentProcessorController {

    @Autowired
    private DocumentProcessorService documentProcessorService;

    @Operation(
        summary = "Process a single document",
        description = "Upload and process a single portfolio document"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Document processed successfully",
            content = @Content(schema = @Schema(implementation = DocumentProcessResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(value = "/process", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentProcessResponse> processDocument(
            @Parameter(description = "Portfolio document file to process", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Type of document being processed", required = true)
            @RequestParam("documentType") DocumentType documentType) {
        return ResponseEntity.ok(documentProcessorService.processDocument(file, documentType));
    }

    @Operation(
        summary = "Process multiple documents",
        description = "Upload and process multiple portfolio documents"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Documents processed successfully",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = DocumentProcessResponse.class)))),
        @ApiResponse(responseCode = "400", description = "Invalid input parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(value = "/batch-process", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<DocumentProcessResponse>> processBatchDocuments(
            @Parameter(description = "List of portfolio document files to process", required = true)
            @RequestParam("files") List<MultipartFile> files,
            @Parameter(description = "Type of documents being processed", required = true)
            @RequestParam("documentType") DocumentType documentType) {
        return ResponseEntity.ok(documentProcessorService.processBatchDocuments(files, documentType));
    }

    @Operation(
        summary = "Get document processing status",
        description = "Retrieve the current status of a document processing request"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Processing status retrieved successfully",
            content = @Content(schema = @Schema(implementation = ProcessingStatus.class))),
        @ApiResponse(responseCode = "404", description = "Process ID not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/status/{processId}")
    public ResponseEntity<ProcessingStatus> getProcessingStatus(
            @Parameter(description = "Unique identifier of the processing request", required = true)
            @PathVariable UUID processId) {
        return ResponseEntity.ok(documentProcessorService.getProcessingStatus(processId));
    }

    @Operation(
        summary = "Get supported document types",
        description = "Retrieve list of supported document types"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Document types retrieved successfully",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/types")
    public ResponseEntity<List<String>> getSupportedDocumentTypes() {
        return ResponseEntity.ok(documentProcessorService.getSupportedDocumentTypes());
    }
}
