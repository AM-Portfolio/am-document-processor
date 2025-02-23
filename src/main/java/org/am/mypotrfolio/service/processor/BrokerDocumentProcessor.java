package org.am.mypotrfolio.service.processor;

import org.am.mypotrfolio.model.DocumentProcessResponse;
import org.springframework.web.multipart.MultipartFile;

public interface BrokerDocumentProcessor {
    DocumentProcessResponse processDocument(MultipartFile file);
    String getBrokerType();
}
