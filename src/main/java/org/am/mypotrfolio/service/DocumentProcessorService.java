package org.am.mypotrfolio.service;

import org.am.mypotrfolio.model.DocumentProcessResponse;
import org.am.mypotrfolio.model.ProcessingStatus;
import org.am.mypotrfolio.service.processor.BrokerProcessorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DocumentProcessorService {
    private static final Logger log = LoggerFactory.getLogger(DocumentProcessorService.class);

    private final Map<UUID, ProcessingStatus> processStatusMap = new ConcurrentHashMap<>();
    private final BrokerProcessorFactory brokerProcessorFactory;

    public DocumentProcessorService(BrokerProcessorFactory brokerProcessorFactory) {
        this.brokerProcessorFactory = brokerProcessorFactory;
    }

    public DocumentProcessResponse processDocument(MultipartFile file, String documentType) {
        UUID processId = UUID.randomUUID();
        log.info("[ProcessId: {}] Starting document processing for type: {}", processId, documentType);
        processStatusMap.put(processId, ProcessingStatus.QUEUED);
        
        try {
            if (documentType.equals("BROKER_PORTFOLIO")) {
                // Extract broker type from file name or content
                log.debug("[ProcessId: {}] Detecting broker type from file", processId);
                String brokerType = detectBrokerType(file);
                log.info("[ProcessId: {}] Detected broker type: {}", processId, brokerType);
                
                var processor = brokerProcessorFactory.getProcessor(brokerType);
                
                processStatusMap.put(processId, ProcessingStatus.PROCESSING);
                log.info("[ProcessId: {}] Processing document with {} processor", processId, brokerType);
                DocumentProcessResponse response = processor.processDocument(file, processId);
                response.setProcessId(processId);
                response.setStatus(ProcessingStatus.COMPLETED);
                processStatusMap.put(processId, ProcessingStatus.COMPLETED);
                
                log.info("[ProcessId: {}] Successfully completed document processing", processId);
                return response;
            } 
            
            else {
                // Handle other document types (mutual funds, NPS, etc.)
                log.info("[ProcessId: {}] Processing non-broker document type: {}", processId, documentType);
                return processOtherDocumentTypes(processId, file, documentType);
            }
        } catch (Exception e) {
            log.error("[ProcessId: {}] Failed to process document: {}", processId, e.getMessage(), e);
            processStatusMap.put(processId, ProcessingStatus.FAILED);
            throw new RuntimeException("Failed to process document: " + e.getMessage(), e);
        }
    }

    public List<DocumentProcessResponse> processBatchDocuments(List<MultipartFile> files, String documentType) {
        UUID batchId = UUID.randomUUID();
        log.info("[BatchId: {}] Starting batch processing of {} documents", batchId, files.size());
        List<DocumentProcessResponse> responses = new ArrayList<>();
        
        for (MultipartFile file : files) {
            responses.add(processDocument(file, documentType));
        }
        
        log.info("[BatchId: {}] Completed batch processing", batchId);
        return responses;
    }

    public ProcessingStatus getProcessingStatus(UUID processId) {
        return processStatusMap.getOrDefault(processId, ProcessingStatus.FAILED);
    }

    public List<String> getSupportedDocumentTypes() {
        List<String> types = new ArrayList<>();
        types.add("BROKER_PORTFOLIO");
        types.add("MUTUAL_FUND");
        types.add("NPS_STATEMENT");
        types.add("COMPANY_FINANCIAL_REPORT");
        types.add("STOCK_PORTFOLIO");
        types.add("NSE_INDICES");
        return types;
    }

    private String detectBrokerType(MultipartFile file) {
        String filename = file.getOriginalFilename().toUpperCase();
        if (filename.contains("DHAN")) {
            return "DHAN";
        } else if (filename.contains("ZERODHA")) {
            return "ZERODHA";
        }
        else if (filename.contains("MSTOCK")) {
            return "MSTOCK";
        }
        throw new IllegalArgumentException("Unable to detect broker type from file: " + filename);
    }

    private DocumentProcessResponse processOtherDocumentTypes(UUID processId, MultipartFile file, String documentType) {
        log.info("[ProcessId: {}] Processing other document type: {}", processId, documentType);
        DocumentProcessResponse response = new DocumentProcessResponse();
        response.setProcessId(processId);
        response.setDocumentType(documentType);
        response.setFileName(file.getOriginalFilename());
        response.setStatus(ProcessingStatus.COMPLETED);
        response.setMessage("Processed " + documentType);
        log.info("[ProcessId: {}] Completed processing other document type", processId);
        return response;
    }
}
