package com.bkent.csvparser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.bkent.csvparser"})
public class CSVParser {
    public static void main(String[] args) {
        SpringApplication.run(CSVParser.class, args);
    }
}
