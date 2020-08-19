package com.bkent.csvparser.service;

import com.bkent.csvparser.component.ApiErrorWarning;
import com.bkent.csvparser.component.MetaData;
import com.bkent.csvparser.component.ResponseWrapper;
import com.bkent.csvparser.config.ApplicationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class CSVParseService {

    private final ApplicationProperties applicationProperties;

    public CSVParseService(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    public ResponseWrapper parseString(String csvData) {
        String transactionId = UUID.randomUUID().toString();
        csvData = handleEscapedQuotations(csvData, transactionId);
        StringBuilder finalString = new StringBuilder();
        try {
            csvData.lines().forEach(line -> finalString.append(handleIndividualLine(line)).append("\n"));
        } catch (IllegalStateException e) {
            return new ResponseWrapper(new MetaData(transactionId),
                    null,
                    HttpStatus.BAD_REQUEST.toString(),
                    Collections.singletonList(new ApiErrorWarning(null, "Invalid quotations",
                            "Quotation marks must appear in pairs. If you want to include a literal quote " +
                                    "in your data, please escape it with an additional quote.", e.getMessage())),
                    null);
        }
        return new ResponseWrapper(new MetaData(transactionId),
                finalString.toString().trim(),
                HttpStatus.OK.toString(),
                null,
                null);
    }

    private String handleEscapedQuotations(String originalString, String transactionId) {
        log.info("Beginning handleEscapedQuotations for transactionId: " + transactionId);
        AtomicInteger counter = new AtomicInteger(0);
        String[] finalChars = {""};
        originalString.chars().mapToObj(character -> (char) character).forEach(character -> {
            counter.getAndIncrement();
            if (character.equals('"') && originalString.startsWith("\"", counter.get())) {
                finalChars[0] += applicationProperties.getQuotationPlaceholder();
            } else if (character.equals('"') && counter.get() > 1 && originalString.substring(counter.get() -2, counter.get() -1).equals("\"")) {
                finalChars[0] += "";
            }
            else {
                finalChars[0] += character;
            }
        });
        log.info("Finished handleEscapedQuotations for transactionId: " + transactionId);
        return finalChars[0];
    }

    private String handleIndividualLine(String line) {
        long countQuotes = line.chars().filter(ch -> ch == '"').count();
        if (countQuotes % 2 != 0) {
            throw new IllegalStateException(line);
        }
        StringBuilder finishedLine = new StringBuilder();
        String quotesReplaced = removeQuotations(line);
        finishedLine.append(breakOnCommas(quotesReplaced));
        return finishedLine.toString();
    }

    private String removeQuotations(String string) {
        List<String> stringsWithoutQuotes = new ArrayList<>();
        AtomicInteger quoteCounter = new AtomicInteger(0);
        AtomicBoolean isOpeningQuote = new AtomicBoolean(false);
        String[] groupedChars = {""};
        string.chars().mapToObj(character -> (char) character).forEach(character -> {
            if (character.equals('"')) {
                quoteCounter.getAndIncrement();
                isOpeningQuote.set(quoteCounter.get() % 2 != 0);
                if (!isOpeningQuote.get()) {
                    stringsWithoutQuotes.add(groupedChars[0]
                            .replace(applicationProperties.getQuotationPlaceholder(), "\""));
                    groupedChars[0] = "";
                }
            } else if (isOpeningQuote.get() && character.equals(',')){
                groupedChars[0] += applicationProperties.getCommaPlaceholder();
            } else {
                groupedChars[0] += character;
            }
        });
        if(!StringUtils.isEmpty(groupedChars[0])) {
            stringsWithoutQuotes.add(groupedChars[0]);
        }
        return String.join("", stringsWithoutQuotes);
    }

    private String breakOnCommas(String string) {
        List<String> strings = new ArrayList<>();
        Arrays.stream(string.split(",")).forEach(item -> strings.add("[" + item.replace("¡", ",") + "]"));
        return String.join(" ", strings);
    }

}