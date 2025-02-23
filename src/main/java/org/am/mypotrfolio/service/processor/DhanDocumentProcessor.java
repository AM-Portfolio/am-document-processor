package org.am.mypotrfolio.service.processor;

import org.am.mypotrfolio.model.DocumentProcessResponse;
import org.am.mypotrfolio.service.DhanService;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.am.common.amcommondata.model.PortfolioModel;

@Component
public class DhanDocumentProcessor implements BrokerDocumentProcessor {
    private final DhanService dhanService;

    public DhanDocumentProcessor(DhanService dhanService) {
        this.dhanService = dhanService;
    }

    @Override
    public DocumentProcessResponse processDocument(MultipartFile file) {
        // Call DhanService specific processing logic
        PortfolioModel portfolioModel = dhanService.processPortfolioFile(file);
        
        // Create response
        DocumentProcessResponse response = new DocumentProcessResponse();
        response.setDocumentType("DHAN_PORTFOLIO");
        response.setFileName(file.getOriginalFilename());
        return response;
    }

    @Override
    public String getBrokerType() {
        return "DHAN";
    }
}
