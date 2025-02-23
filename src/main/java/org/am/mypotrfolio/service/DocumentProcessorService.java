package org.am.mypotrfolio.service;

import org.am.mypotrfolio.model.DocumentProcessResponse;
import org.am.mypotrfolio.model.ProcessingStatus;
import org.am.mypotrfolio.service.processor.BrokerProcessorFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DocumentProcessorService {
    private final Map<UUID, ProcessingStatus> processStatusMap = new ConcurrentHashMap<>();
    private final BrokerProcessorFactory brokerProcessorFactory;

    public DocumentProcessorService(BrokerProcessorFactory brokerProcessorFactory) {
        this.brokerProcessorFactory = brokerProcessorFactory;
    }

    public DocumentProcessResponse processDocument(MultipartFile file, String documentType) {
        UUID processId = UUID.randomUUID();
        processStatusMap.put(processId, ProcessingStatus.QUEUED);
        
        try {
            if (documentType.equals("BROKER_PORTFOLIO")) {
                // Extract broker type from file name or content
                String brokerType = detectBrokerType(file);
                var processor = brokerProcessorFactory.getProcessor(brokerType);
                
                processStatusMap.put(processId, ProcessingStatus.PROCESSING);
                DocumentProcessResponse response = processor.processDocument(file);
                response.setProcessId(processId);
                response.setStatus(ProcessingStatus.COMPLETED);
                processStatusMap.put(processId, ProcessingStatus.COMPLETED);
                
                return response;
            } 
            
            else {
                // Handle other document types (mutual funds, NPS, etc.)
                return processOtherDocumentTypes(processId, file, documentType);
            }
        } catch (Exception e) {
            processStatusMap.put(processId, ProcessingStatus.FAILED);
            throw new RuntimeException("Failed to process document: " + e.getMessage(), e);
        }
    }

    public List<DocumentProcessResponse> processBatchDocuments(List<MultipartFile> files, String documentType) {
        List<DocumentProcessResponse> responses = new ArrayList<>();
        
        for (MultipartFile file : files) {
            responses.add(processDocument(file, documentType));
        }
        
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
        // TODO: Implement broker detection logic based on file content or name
        // For now, we'll assume it's in the filename: e.g., "dhan_portfolio.pdf"
        String filename = file.getOriginalFilename().toUpperCase();
        if (filename.contains("DHAN")) {
            return "DHAN";
        } else if (filename.contains("ZERODHA")) {
            return "ZERODHA";
        }
        throw new IllegalArgumentException("Unable to detect broker type from file: " + filename);
    }

    private DocumentProcessResponse processOtherDocumentTypes(UUID processId, MultipartFile file, String documentType) {
        // TODO: Implement processing logic for other document types
        DocumentProcessResponse response = new DocumentProcessResponse();
        response.setProcessId(processId);
        response.setDocumentType(documentType);
        response.setFileName(file.getOriginalFilename());
        response.setStatus(ProcessingStatus.COMPLETED);
        response.setMessage("Processed " + documentType);
        return response;
    }
}
