package org.am.mypotrfolio.service;

import java.util.Set;

import org.am.mypotrfolio.domain.common.PortfolioRequest;

import com.am.common.amcommondata.model.asset.AssetModel;
import com.am.common.amcommondata.model.asset.mutualfund.MutualFundModel;

public interface PortfolioService {

    Set<AssetModel> processEquityFile(PortfolioRequest portfolioRequest);

    Set<MutualFundModel> processMutualFundFile(PortfolioRequest portfolioRequest);

    default Double getDouble(String value) {
        return Double.parseDouble(value.replaceAll(",", ""));
    }
}
