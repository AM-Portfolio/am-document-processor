package org.am.mypotrfolio.model;

import lombok.Data;
import java.util.UUID;

@Data
public class DocumentProcessResponse {
    private UUID processId;
    private String documentType;
    private String fileName;
    private ProcessingStatus status;
    private String message;
}
