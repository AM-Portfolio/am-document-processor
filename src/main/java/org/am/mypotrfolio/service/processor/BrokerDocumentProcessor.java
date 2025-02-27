package org.am.mypotrfolio.service.processor;

import org.am.mypotrfolio.domain.common.PortfolioRequest;
import org.am.mypotrfolio.model.DocumentProcessResponse;

import com.am.common.amcommondata.model.enums.BrokerType;

public interface BrokerDocumentProcessor {
    DocumentProcessResponse processDocument(PortfolioRequest portfolioRequest);
    BrokerType getBrokerType();
}
