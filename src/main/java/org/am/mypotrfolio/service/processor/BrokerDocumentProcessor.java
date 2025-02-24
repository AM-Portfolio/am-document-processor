package org.am.mypotrfolio.service.processor;

import org.am.mypotrfolio.model.DocumentProcessResponse;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

public interface BrokerDocumentProcessor {
    DocumentProcessResponse processDocument(MultipartFile file, UUID processId);
    String getBrokerType();
}
