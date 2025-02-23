package org.am.mypotrfolio.processor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Slf4j
public abstract class AbstractFileProcessor implements FileProcessor {

    @Override
    public List<Map<String, String>> processFile(MultipartFile file, String brokerType) {
        log.info("Processing {} file for broker: {}", getFileType(), brokerType);
        try {
            if (brokerType.equalsIgnoreCase("Zerodha")) {
                log.debug("Using Zerodha parser");
                return parseZerodhaFile(file);
            } else if (brokerType.equalsIgnoreCase("MStock")) {
                log.debug("Using MStock parser");
                return parseMStockFile(file);
            } else if (brokerType.equalsIgnoreCase("Dhan")) {
                log.debug("Using Dhan parser");
                return parseDhanFile(file);
            }
            log.debug("Using default Dhan parser");
            return parseDhanFile(file);
        } catch (Exception e) {
            log.error("Error processing {} file: {}", getFileType(), e.getMessage(), e);
            throw new RuntimeException("Failed to process " + getFileType() + " file", e);
        }
    }

    protected abstract List<Map<String, String>> parseZerodhaFile(MultipartFile file) throws Exception;
    protected abstract List<Map<String, String>> parseMStockFile(MultipartFile file) throws Exception;
    protected abstract List<Map<String, String>> parseDhanFile(MultipartFile file) throws Exception;

    protected Map<String, String> createRowData(String[] headers, String[] values) {
        Map<String, String> row = new LinkedHashMap<>();
        boolean hasData = false;

        for (int i = 0; i < headers.length && i < values.length; i++) {
            String value = values[i].trim();
            if (!value.isEmpty()) {
                hasData = true;
            }
            row.put(headers[i].trim(), value);
        }

        return hasData ? row : null;
    }
}
