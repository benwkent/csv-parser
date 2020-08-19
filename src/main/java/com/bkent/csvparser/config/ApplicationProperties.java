package com.bkent.csvparser.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "com.bkent.csvparser")
public class ApplicationProperties {
    private String quotationPlaceholder;
    private String commaPlaceholder;
}
