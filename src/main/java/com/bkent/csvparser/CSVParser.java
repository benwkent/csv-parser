package com.bkent.csvparser;

import com.bkent.csvparser.config.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = {"com.bkent.csvparser"})
@EnableConfigurationProperties(ApplicationProperties.class)
public class CSVParser {
    public static void main(String[] args) {
        SpringApplication.run(CSVParser.class, args);
    }
}
