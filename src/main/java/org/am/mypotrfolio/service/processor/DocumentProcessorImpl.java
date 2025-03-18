package org.am.mypotrfolio.service.processor;

import java.util.List;
import java.util.Set;

import org.am.mypotrfolio.domain.common.DocumentRequest;
import org.am.mypotrfolio.model.DocumentProcessResponse;
import org.am.mypotrfolio.service.MessagingEventService;
import org.am.mypotrfolio.service.NseService;
import org.am.mypotrfolio.service.PortfolioService;
import org.springframework.stereotype.Component;

import com.am.common.amcommondata.model.asset.AssetModel;
import com.am.common.amcommondata.model.asset.equity.EquityModel;
import com.am.common.amcommondata.model.asset.mutualfund.MutualFundModel;
import com.am.common.amcommondata.model.enums.BrokerType;
import com.am.common.amcommondata.model.security.SecurityModel;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DocumentProcessorImpl implements DocumentProcessor {
    private final PortfolioService portfolioService;
    private final NseService nseService;
    private final MessagingEventService messagingEventService;

    @Override
    public DocumentProcessResponse processDocument(DocumentRequest documentRequest) {
        processPortfolio(documentRequest);
        DocumentProcessResponse response = new DocumentProcessResponse();
        response.setDocumentType(documentRequest.getDocumentType().name());
        response.setFileName(documentRequest.getFile().getOriginalFilename());
        response.setProcessId(documentRequest.getRequestId());
        return response;
    }

    private void processPortfolio(DocumentRequest documentRequest) {
        if(documentRequest.getDocumentType().isStockPortfolio()) {
            processEquityPortfolio(documentRequest);
        } else if(documentRequest.getDocumentType().isMutualFund()) {
            processMutualFundsPortfolio(documentRequest);
        }
        else if(documentRequest.getDocumentType().isNseIndices()) {
            processNseIndices(documentRequest);
        }
    }

    private void processEquityPortfolio(DocumentRequest documentRequest) {
        List<EquityModel> assets = portfolioService.processEquityFile(documentRequest);
        messagingEventService.sendStockPortfolioMessage(assets, documentRequest.getRequestId(), documentRequest.getBrokerType());
    }

    private void processNseIndices(DocumentRequest documentRequest) {
        List<SecurityModel> assets = nseService.processNseSecurity(documentRequest);
        //messagingEventService.sendNseIndicesMessage(assets, documentRequest.getRequestId(), documentRequest.getBrokerType());
    }

    private void processMutualFundsPortfolio(DocumentRequest documentRequest) {
        List<MutualFundModel> mutualFunds = portfolioService.processMutualFundFile(documentRequest);
        messagingEventService.sendMutualFundPortfolioMessage(mutualFunds, documentRequest.getRequestId(), documentRequest.getBrokerType());
    }

    @Override
    public BrokerType getBrokerType() {
        return BrokerType.DHAN;
    }
}
