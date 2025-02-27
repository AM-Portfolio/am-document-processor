package org.am.mypotrfolio.service.processor;

import java.util.Set;
import java.util.UUID;

import org.am.mypotrfolio.domain.common.DocumentType;
import org.am.mypotrfolio.model.DocumentProcessResponse;
import org.am.mypotrfolio.service.MessagingEventService;
import org.am.mypotrfolio.service.PortfolioService;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.am.common.amcommondata.model.asset.AssetModel;
import com.am.common.amcommondata.model.asset.mutualfund.MutualFundModel;
import com.am.common.amcommondata.model.enums.BrokerType;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BrokerDocumentProcessorImpl implements BrokerDocumentProcessor {
    private final PortfolioService portfolioService;
    private final MessagingEventService messagingEventService;

    @Override
    public DocumentProcessResponse processDocument(MultipartFile file, UUID processId, BrokerType brokerType, DocumentType documentType) {
        processPortfolio(file, processId, brokerType, documentType);
        DocumentProcessResponse response = new DocumentProcessResponse();
        response.setDocumentType(documentType.name());
        response.setFileName(file.getOriginalFilename());
        response.setProcessId(processId);
        return response;
    }

    private void processPortfolio(MultipartFile file, UUID processId, BrokerType brokerType, DocumentType documentType) {
        if(documentType.isStockPortfolio()) {
            processEquityPortfolio(file, processId, brokerType);
        } else if(documentType.isMutualFund()) {
            processMutualFundsPortfolio(file, processId, brokerType);
        }
    }

    private void processEquityPortfolio(MultipartFile file, UUID processId, BrokerType brokerType) {
        Set<AssetModel> assets = portfolioService.processEquityFile(file, processId, brokerType);
        messagingEventService.sendStockPortfolioMessage(assets, processId, brokerType);
    }

    private void processMutualFundsPortfolio(MultipartFile file, UUID processId, BrokerType brokerType) {
        Set<MutualFundModel> mutualFunds = portfolioService.processMutualFundFile(file, processId, brokerType);
        messagingEventService.sendMutualFundPortfolioMessage(mutualFunds, processId, brokerType);
    }

    @Override
    public BrokerType getBrokerType() {
        return BrokerType.DHAN;
    }
}
