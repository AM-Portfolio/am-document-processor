package org.am.mypotrfolio.service.processor;

import java.util.Set;
import java.util.UUID;

import org.am.mypotrfolio.model.DocumentProcessResponse;
import org.am.mypotrfolio.service.MessagingEventService;
import org.am.mypotrfolio.service.ZerodhaService;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.am.common.amcommondata.model.asset.AssetModel;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ZerodhaDocumentProcessor implements BrokerDocumentProcessor {
    private final ZerodhaService zerodhaService;
    private final MessagingEventService messagingEventService;

    @Override
    public DocumentProcessResponse processDocument(MultipartFile file, UUID processId) {
        // Call ZerodhaService specific processing logic
        Set<AssetModel> assets = zerodhaService.processPortfolioFile(file, processId);
        messagingEventService.sendMessage(assets, processId);
        // Create response
        DocumentProcessResponse response = new DocumentProcessResponse();
        response.setDocumentType("ZERODHA_PORTFOLIO");
        response.setFileName(file.getOriginalFilename());
        response.setProcessId(processId);
        return response;
    }

    @Override
    public String getBrokerType() {
        return "ZERODHA";
    }
}
