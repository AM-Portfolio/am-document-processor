package org.am.mypotrfolio;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@Slf4j
@SpringBootApplication
@ComponentScan(basePackages = "org.am.mypotrfolio")
public class DocumentProcessingApplication {

    public static void main(String[] args) {
        log.info("Starting Document Processing Application...");
        try {
            ConfigurableApplicationContext context = SpringApplication.run(DocumentProcessingApplication.class, args);
            log.info("Document Processing Application started successfully");
            log.debug("Active profiles: {}", String.join(", ", context.getEnvironment().getActiveProfiles()));
        } catch (Exception e) {
            log.error("Failed to start Document Processing Application: {}", e.getMessage(), e);
            throw e;
        }
    }
}
