package org.am.mypotrfolio.service;

import org.springframework.web.multipart.MultipartFile;

import com.am.common.amcommondata.model.PortfolioModel;

public interface PortfolioService {

    PortfolioModel processPortfolioFile(MultipartFile fileName);
    
}
