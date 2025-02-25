package org.am.mypotrfolio.kafka.model;

import java.util.Set;
import java.util.UUID;

import com.am.common.amcommondata.model.asset.AssetModel;
import com.am.common.amcommondata.model.enums.BrokerType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioUpdateEvent {
    private UUID id;
    private BrokerType brokerType;
    private String userId;
    private Set<AssetModel> assets;
    private long timestamp;
}
