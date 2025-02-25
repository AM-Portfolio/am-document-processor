package org.am.mypotrfolio.service;

import java.util.Set;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.am.common.amcommondata.model.asset.AssetModel;

public interface PortfolioService {

    Set<AssetModel> processPortfolioFile(MultipartFile fileName, UUID processId);
    
    default Double getDouble(String value) {
        return Double.parseDouble(value.replaceAll(",", ""));
    }
}
