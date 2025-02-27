package org.am.mypotrfolio.service.processor;

import java.util.Set;
import java.util.UUID;

import org.am.mypotrfolio.domain.common.DocumentType;
import org.am.mypotrfolio.domain.common.PortfolioRequest;
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
    public DocumentProcessResponse processDocument(PortfolioRequest portfolioRequest) {
        processPortfolio(portfolioRequest);
        DocumentProcessResponse response = new DocumentProcessResponse();
        response.setDocumentType(portfolioRequest.getDocumentType().name());
        response.setFileName(portfolioRequest.getFile().getOriginalFilename());
        response.setProcessId(portfolioRequest.getRequestId());
        return response;
    }

    private void processPortfolio(PortfolioRequest portfolioRequest) {
        if(portfolioRequest.getDocumentType().isStockPortfolio()) {
            processEquityPortfolio(portfolioRequest);
        } else if(portfolioRequest.getDocumentType().isMutualFund()) {
            processMutualFundsPortfolio(portfolioRequest);
        }
    }

    private void processEquityPortfolio(PortfolioRequest portfolioRequest) {
        Set<AssetModel> assets = portfolioService.processEquityFile(portfolioRequest);
        messagingEventService.sendStockPortfolioMessage(assets, portfolioRequest.getRequestId(), portfolioRequest.getBrokerType());
    }

    private void processMutualFundsPortfolio(PortfolioRequest portfolioRequest) {
        Set<MutualFundModel> mutualFunds = portfolioService.processMutualFundFile(portfolioRequest);
        messagingEventService.sendMutualFundPortfolioMessage(mutualFunds, portfolioRequest.getRequestId(), portfolioRequest.getBrokerType());
    }

    @Override
    public BrokerType getBrokerType() {
        return BrokerType.DHAN;
    }
}
