package org.am.mypotrfolio.controller;

import org.am.mypotrfolio.model.DocumentProcessResponse;
import org.am.mypotrfolio.model.ProcessingStatus;
import org.am.mypotrfolio.service.DocumentProcessorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentProcessorController {

    @Autowired
    private DocumentProcessorService documentProcessorService;

    @PostMapping("/process")
    public ResponseEntity<DocumentProcessResponse> processDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") String documentType) {
        return ResponseEntity.ok(documentProcessorService.processDocument(file, documentType));
    }

    @PostMapping("/batch-process")
    public ResponseEntity<List<DocumentProcessResponse>> processBatchDocuments(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("documentType") String documentType) {
        return ResponseEntity.ok(documentProcessorService.processBatchDocuments(files, documentType));
    }

    @GetMapping("/status/{processId}")
    public ResponseEntity<ProcessingStatus> getProcessingStatus(@PathVariable UUID processId) {
        return ResponseEntity.ok(documentProcessorService.getProcessingStatus(processId));
    }

    @GetMapping("/types")
    public ResponseEntity<List<String>> getSupportedDocumentTypes() {
        return ResponseEntity.ok(documentProcessorService.getSupportedDocumentTypes());
    }
}
