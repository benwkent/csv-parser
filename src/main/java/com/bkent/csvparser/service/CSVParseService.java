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
            csvData.lines().forEach(line -> finalString.append(handleIndividualLine(line, transactionId)).append("\n"));
        } catch (IllegalStateException e) {
            return new ResponseWrapper(new MetaData(transactionId),
                    null,
                    HttpStatus.BAD_REQUEST.value(),
                    Collections.singletonList(new ApiErrorWarning(null, "Invalid quotations",
                            "Quotation marks must appear in pairs. If you want to include a literal quote " +
                                    "in your data, please escape it with an additional quote.", e.getMessage())),
                    null);
        }
        return new ResponseWrapper(new MetaData(transactionId),
                finalString.toString().trim(),
                HttpStatus.OK.value(),
                null,
                null);
    }

    private String handleEscapedQuotations(String originalString, String transactionId) {
        log.info("Beginning handleEscapedQuotations for transactionId: " + transactionId);
        AtomicInteger counter = new AtomicInteger(0);
        String[] finalChars = {""};
        originalString.chars().mapToObj(character -> (char) character).forEach(character -> {
            counter.getAndIncrement();
            if (character.equals('"') && nextCharIsQuote(originalString, counter.get())) {
                finalChars[0] += applicationProperties.getQuotationPlaceholder();
            } else if (character.equals('"') && previousCharIsQuote(originalString, counter.get())) {
                finalChars[0] += "";
            } else {
                finalChars[0] += character;
            }
        });
        log.info("Finished handleEscapedQuotations for transactionId: " + transactionId);
        return finalChars[0];
    }

    private boolean nextCharIsQuote(String originalString, int counter) {
        return originalString.startsWith("\"", counter);
    }

    private boolean previousCharIsQuote(String originalString, int counter) {
        return counter > 1 && originalString.substring(counter - 2, counter - 1).equals("\"");
    }

    private String handleIndividualLine(String line, String transactionId) {
        log.info("Beginning individual line processing for line " + line + " for transactionId: " + transactionId);
        long countQuotes = line.chars().filter(character -> character == '"').count();
        if (countQuotes % 2 != 0) {
            log.error("An odd number of quotations was present in the line " + line + " for transactionId: " + transactionId);
            throw new IllegalStateException(line);
        }
        StringBuilder finishedLine = new StringBuilder();
        String quotesReplaced = removeQuotations(line, transactionId);
        finishedLine.append(breakOnCommas(quotesReplaced));
        log.info("Finished individual line processing for line " + line + " for transactionId: " + transactionId);
        return finishedLine.toString();
    }

    private String removeQuotations(String line, String transactionId) {
        log.info("Beginning adding quotation placeholders for for line " + line + " for transactionId: " + transactionId);
        List<String> stringsWithoutQuotes = new ArrayList<>();
        AtomicInteger quoteCounter = new AtomicInteger(0);
        AtomicBoolean isOpeningQuote = new AtomicBoolean(false);
        String[] csvItemBuilder = {""};
        line.chars().mapToObj(character -> (char) character).forEach(character -> {
            if (character.equals('"')) {
                quoteCounter.getAndIncrement();
                isOpeningQuote.set(quoteCounter.get() % 2 != 0);
                if (!isOpeningQuote.get()) {
                    stringsWithoutQuotes.add(csvItemBuilder[0]
                            .replace(applicationProperties.getQuotationPlaceholder(), "\""));
                    csvItemBuilder[0] = "";
                }
            } else if (isOpeningQuote.get() && character.equals(',')) {
                csvItemBuilder[0] += applicationProperties.getCommaPlaceholder();
            } else {
                csvItemBuilder[0] += character;
            }
        });
        if (!StringUtils.isEmpty(csvItemBuilder[0])) {
            stringsWithoutQuotes.add(csvItemBuilder[0]);
        }
        log.info("Done adding quotation placeholders for for line " + line + " for transactionId: " + transactionId);
        return String.join("", stringsWithoutQuotes);
    }

    private String breakOnCommas(String line) {
        List<String> finishedLineItems = new ArrayList<>();
        Arrays.stream(line.split(","))
                .forEach(item -> finishedLineItems.add("[" + item.replace(applicationProperties.getCommaPlaceholder(), ",") + "]"));
        return String.join(" ", finishedLineItems);
    }

}