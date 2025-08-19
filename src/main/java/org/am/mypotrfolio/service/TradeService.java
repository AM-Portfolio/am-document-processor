package org.am.mypotrfolio.service;

import java.util.List;

import org.am.mypotrfolio.domain.common.DocumentRequest;
import org.am.mypotrfolio.model.trade.TradeModel;

/**
 * Service interface for processing trade-related documents
 */
public interface TradeService {

    List<TradeModel> processTradeFile(DocumentRequest documentRequest);
}
