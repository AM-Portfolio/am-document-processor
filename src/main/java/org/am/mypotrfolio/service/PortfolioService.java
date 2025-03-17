package org.am.mypotrfolio.service;

import java.util.Set;

import org.am.mypotrfolio.domain.common.DocumentRequest;

import com.am.common.amcommondata.model.asset.AssetModel;
import com.am.common.amcommondata.model.asset.mutualfund.MutualFundModel;

public interface PortfolioService {

    Set<AssetModel> processEquityFile(DocumentRequest portfolioRequest);

    Set<MutualFundModel> processMutualFundFile(DocumentRequest portfolioRequest);

    default Double getDouble(String value) {
        return Double.parseDouble(value.replaceAll(",", ""));
    }
}
