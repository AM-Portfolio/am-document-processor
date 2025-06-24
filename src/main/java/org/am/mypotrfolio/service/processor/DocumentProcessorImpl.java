package org.am.mypotrfolio.service.processor;

import java.util.List;

import org.am.mypotrfolio.domain.common.DocumentRequest;
import org.am.mypotrfolio.model.DocumentProcessResponse;
import org.am.mypotrfolio.model.trade.TradeModel;
import org.am.mypotrfolio.service.MessagingEventService;
import org.am.mypotrfolio.service.NseService;
import org.am.mypotrfolio.service.PortfolioService;
import org.am.mypotrfolio.service.TradeService;
import org.springframework.stereotype.Component;

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
    private final TradeService tradeService;

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
        } else if(documentRequest.getDocumentType().isNseIndices()) {
            processNseIndices(documentRequest);
        } else if(documentRequest.getDocumentType().isTradeFno()) {
            processTradeFno(documentRequest);
        } else if(documentRequest.getDocumentType().isTradeEq()) {
            processTradeEq(documentRequest);
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

    private void processTradeFno(DocumentRequest documentRequest) {
        List<TradeModel> trades = tradeService.processTradeFile(documentRequest);
        messagingEventService.sendTradeFnoMessage(trades, documentRequest.getRequestId(), documentRequest.getBrokerType());
    }

    private void processTradeEq(DocumentRequest documentRequest) {
        List<TradeModel> trades = tradeService.processTradeFile(documentRequest);
        messagingEventService.sendTradeEqMessage(trades, documentRequest.getRequestId(), documentRequest.getBrokerType());
    }

    @Override
    public BrokerType getBrokerType() {
        return BrokerType.DHAN;
    }
}
