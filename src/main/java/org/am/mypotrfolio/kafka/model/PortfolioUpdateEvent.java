package org.am.mypotrfolio.kafka.model;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.am.common.amcommondata.model.asset.AssetModel;
import com.am.common.amcommondata.model.asset.equity.EquityModel;
import com.am.common.amcommondata.model.asset.mutualfund.MutualFundModel;
import com.am.common.amcommondata.model.enums.BrokerType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class PortfolioUpdateEvent {
    private UUID id;
    private BrokerType brokerType;
    private String userId;
    private List<EquityModel> assets;
    private List<MutualFundModel> mutualFunds;
    private long timestamp;
}
