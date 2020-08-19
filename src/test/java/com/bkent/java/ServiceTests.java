package com.bkent.java;

import com.bkent.csvparser.config.ApplicationProperties;
import com.bkent.csvparser.service.CSVParseService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration(classes = {CSVParseService.class, ApplicationProperties.class})
public class ServiceTests {

    @Autowired
    private CSVParseService csvParseService;
    @Autowired
    private ApplicationProperties applicationProperties;

    @Before
    public void setApplicationProperties() {
        applicationProperties.setCommaPlaceholder("¡");
        applicationProperties.setQuotationPlaceholder("™");
    }

    @Test
    public void testCommasWithinQuotes() {
        String input = "\"Name, test\",\"other\"";
        String expectedResult = "[Name, test] [other]";
        assertEquals(expectedResult, csvParseService.parseString(input));
    }

    @Test
    public void testMultipleLines() {
        String input = "\"Name, test\",\"other\"\n\"Second\",\"line\"";
        String expectedResult = "[Name, test] [other]\n[Second] [line]";
        assertEquals(expectedResult, csvParseService.parseString(input));
    }

    @Test
    public void testMixtureOfStringAndNonString() {
        String input = "\"Name, test\",12,\"Next\"";
        String expectedResult = "[Name, test] [12] [Next]";
        assertEquals(expectedResult, csvParseService.parseString(input));
    }

    @Test
    public void testEndNonString() {
        String input = "\"Name, test\",12";
        String expectedResult = "[Name, test] [12]";
        assertEquals(expectedResult, csvParseService.parseString(input));
    }

    @Test
    public void testStartNonString() {
        String input = "12,\"Name, test\"";
        String expectedResult = "[12] [Name, test]";
        assertEquals(expectedResult, csvParseService.parseString(input));
    }

    @Test
    public void testEmptyElement() {
        String input = "12,,\"Name, test\"";
        String expectedResult = "[12] [] [Name, test]";
        assertEquals(expectedResult, csvParseService.parseString(input));
    }

    @Test
    public void testEscapedQuote() {
        String input = "12,,\"Name\"\", test\"";
        String expectedResult = "[12] [] [Name\", test]";
        assertEquals(expectedResult, csvParseService.parseString(input));
    }

    @Test
    public void testGivenData() {
        String input = "\"Patient Name\",\"SSN\",\"Age\",\"Phone Number\",\"Status\"\n" +
                        "\"Prescott, Zeke\",\"542-51-6641\",21,\"801-555-2134\",\"Opratory=2,PCP=1\"\n" +
                        "\"Goldstein, Bucky\",\"635-45-1254\",42,\"435-555-1541\",\"Opratory=1,PCP=1\"\n" +
                        "\"Vox, Bono\",\"414-45-1475\",51,\"801-555-2100\",\"Opratory=3,PCP=2\"";
        String expectedResult = "[Patient Name] [SSN] [Age] [Phone Number] [Status]\n" +
                                "[Prescott, Zeke] [542-51-6641] [21] [801-555-2134] [Opratory=2,PCP=1]\n" +
                                "[Goldstein, Bucky] [635-45-1254] [42] [435-555-1541] [Opratory=1,PCP=1]\n" +
                                "[Vox, Bono] [414-45-1475] [51] [801-555-2100] [Opratory=3,PCP=2]";
        assertEquals(expectedResult, csvParseService.parseString(input));
    }

    @Test
    public void testErrorWhenQuotesAreUneven() {
        assertEquals("Quotation marks must appear in pairs. If you want to include a literal quote in your " +
                "data, please escape it with an additional quote.",csvParseService.parseString("\"string"));
    }
}
