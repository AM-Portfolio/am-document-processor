package org.am.mypotrfolio.processor;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

public interface FileProcessor {
    List<Map<String, String>> processFile(MultipartFile file, String brokerType);
    String getFileType();
    boolean canProcess(String fileExtension);
}
