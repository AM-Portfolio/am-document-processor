package org.am.mypotrfolio.service.processor;

import java.util.Set;
import java.util.UUID;

import org.am.mypotrfolio.model.DocumentProcessResponse;
import org.am.mypotrfolio.service.DhanService;
import org.am.mypotrfolio.service.MessagingEventService;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.am.common.amcommondata.model.asset.AssetModel;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DhanDocumentProcessor implements BrokerDocumentProcessor {
    private final DhanService dhanService;
    private final MessagingEventService messagingEventService;

    @Override
    public DocumentProcessResponse processDocument(MultipartFile file, UUID processId) {
        // Call DhanService specific processing logic
        Set<AssetModel> assets = dhanService.processPortfolioFile(file, processId);
        messagingEventService.sendMessage(assets, processId);
        // Create response
        DocumentProcessResponse response = new DocumentProcessResponse();
        response.setDocumentType("DHAN_PORTFOLIO");
        response.setFileName(file.getOriginalFilename());
        response.setProcessId(processId);
        return response;
    }

    @Override
    public String getBrokerType() {
        return "DHAN";
    }
}
