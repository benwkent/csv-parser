package com.bkent.csvparser.controller;

import com.bkent.csvparser.service.CSVParseService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CSVController {

    private final CSVParseService parseService;

    public CSVController(CSVParseService parseService) {
        this.parseService = parseService;
    }

    @PostMapping("/parseString")
    public String parseCsvString(@RequestBody String csvData) {
        return parseService.parseString(csvData);
    }
}
