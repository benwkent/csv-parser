package com.bkent.csvparser.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class CSVParseService {

    private static final char INTERNAL_COMMA_PLACEHOLDER = '¡';
    private static final char ESCAPED_QUOTATION = '™';

    public String parseString(String csvData) {
        csvData = handleEscapedQuotations(csvData);
        StringBuilder finalString = new StringBuilder();
        try {
            csvData.lines().forEach(line -> finalString.append(handleIndividualLine(line)).append("\n"));
        } catch (IllegalStateException e) {
            return e.getMessage();
        }
        return finalString.toString().trim();
    }

    private String handleEscapedQuotations(String originalString) {
        AtomicInteger counter = new AtomicInteger(0);
        String[] finalChars = {""};
        originalString.chars().mapToObj(character -> (char) character).forEach(character -> {
            counter.getAndIncrement();
            if (character.equals('"') && originalString.startsWith("\"", counter.get())) {
                finalChars[0] += ESCAPED_QUOTATION;
            } else if (character.equals('"') && counter.get() > 1 && originalString.substring(counter.get() -2, counter.get() -1).equals("\"")) {
                finalChars[0] += "";
            }
            else {
                finalChars[0] += character;
            }
        });
        return finalChars[0];
    }

    private String handleIndividualLine(String line) {
        long countQuotes = line.chars().filter(ch -> ch == '"').count();
        if (countQuotes % 2 != 0) {
            throw new IllegalStateException("Quotation marks must appear in pairs. " +
                    "If you want to include a literal quote in your data, please escape it with an additional quote.");
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
                    stringsWithoutQuotes.add(groupedChars[0].replace(ESCAPED_QUOTATION, '"'));
                    groupedChars[0] = "";
                }
            } else if (isOpeningQuote.get() && character.equals(',')){
                groupedChars[0] += INTERNAL_COMMA_PLACEHOLDER;
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