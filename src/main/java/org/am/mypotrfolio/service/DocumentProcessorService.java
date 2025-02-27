package org.am.mypotrfolio.service;

import org.am.mypotrfolio.domain.common.DocumentType;
import org.am.mypotrfolio.domain.common.PortfolioRequest;
import org.am.mypotrfolio.model.DocumentProcessResponse;
import org.am.mypotrfolio.model.ProcessingStatus;
import org.am.mypotrfolio.service.processor.BrokerDocumentProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.am.common.amcommondata.model.enums.BrokerType;

import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class DocumentProcessorService {
    private static final Logger log = LoggerFactory.getLogger(DocumentProcessorService.class);

    private final Map<UUID, ProcessingStatus> processStatusMap = new ConcurrentHashMap<>();
    private final BrokerDocumentProcessor brokerDocumentProcessor;

    public DocumentProcessResponse processDocument(MultipartFile file, DocumentType documentType) {
        var portfolioRequest = getPortfolioRequest(file, documentType);
        log.info("[ProcessId: {}] Starting document processing for type: {}", portfolioRequest.getRequestId(), documentType);
        processStatusMap.put(portfolioRequest.getRequestId(), ProcessingStatus.QUEUED);
        
        try {
                // Extract broker type from file name or content
                log.debug("[ProcessId: {}] Detecting broker type from file", portfolioRequest.getRequestId());
                log.info("[ProcessId: {}] Detected broker type: {}", portfolioRequest.getRequestId(), portfolioRequest.getBrokerType());
                
                processStatusMap.put(portfolioRequest.getRequestId(), ProcessingStatus.PROCESSING);
                log.info("[ProcessId: {}] Processing document with {} processor", portfolioRequest.getRequestId(), portfolioRequest.getBrokerType());
                DocumentProcessResponse response = brokerDocumentProcessor.processDocument(portfolioRequest);
                response.setProcessId(portfolioRequest.getRequestId());
                response.setStatus(ProcessingStatus.COMPLETED);
                processStatusMap.put(portfolioRequest.getRequestId(), ProcessingStatus.COMPLETED);
                
                log.info("[ProcessId: {}] Successfully completed document processing", portfolioRequest.getRequestId());
                return response;
        
        } catch (Exception e) {
            log.error("[ProcessId: {}] Failed to process document: {}", portfolioRequest.getRequestId(), e.getMessage(), e);
            processStatusMap.put(portfolioRequest.getRequestId(), ProcessingStatus.FAILED);
            throw new RuntimeException("Failed to process document: " + e.getMessage(), e);
        }
    }

    private PortfolioRequest getPortfolioRequest(MultipartFile file, DocumentType documentType) {
        UUID processId = UUID.randomUUID();
        BrokerType brokerType = detectBrokerType(file);
        return PortfolioRequest.builder().file(file).documentType(documentType).requestId(processId).brokerType(brokerType).build();
    }

    public List<DocumentProcessResponse> processBatchDocuments(List<MultipartFile> files,  DocumentType documentType) {
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

    private BrokerType detectBrokerType(MultipartFile file) {
        String filename = file.getOriginalFilename().toUpperCase();
        if (filename.contains("DHAN")) {
            return BrokerType.DHAN;
        } else if (filename.contains("ZERODHA")) {
            return BrokerType.ZERODHA;
        }
        else if (filename.contains("MSTOCK")) {
            return BrokerType.MSTOCK;
        }
        else if (filename.contains("GROWW")) {
            return BrokerType.GROW;
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
