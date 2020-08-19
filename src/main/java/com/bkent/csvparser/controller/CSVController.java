package com.bkent.csvparser.controller;

import com.bkent.csvparser.component.ResponseWrapper;
import com.bkent.csvparser.service.CSVParseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/parse")
public class CSVController {

    private final CSVParseService parseService;

    public CSVController(CSVParseService parseService) {
        this.parseService = parseService;
    }

    @Operation(summary = "Parse CSV string",
            description = "A string of CSV is translated, with each item being placed in brackets ([]). If quotation " +
                    "marks are desired to be parsed, make sure to escape them with an additional quotation mark.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully translated data",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseWrapper.class))),
            @ApiResponse(responseCode = "400", description = "Invalid Quotation Marks",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseWrapper.class)))
    })
    @PostMapping("/string")
    public ResponseWrapper parseCsvString(@RequestBody String csvData) {
        return parseService.parseString(csvData);
    }
}
