package cds.adql.validation.report;

import adql.parser.ADQLParser;
import cds.adql.validation.ValidationException;
import cds.adql.validation.query.Contact;
import cds.adql.validation.query.Publisher;
import cds.adql.validation.query.ValidationQuery;
import cds.adql.validation.query.ValidationSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNull;

class MarkdownReportTest {

    private final static String LINE_SEP = System.lineSeparator();

    private MarkdownReport report = null;
    private ByteArrayOutputStream out = null;
    private ByteArrayOutputStream err = null;

    @BeforeEach
    void setUp() {
        out = new ByteArrayOutputStream();
        err = new ByteArrayOutputStream();
        report = new MarkdownReport(new PrintStream(out), new PrintStream(err));
    }

    @AfterEach
    void tearDown() {
        // Close the output stream:
        try {
            out.close();
        } catch (IOException e) {
            System.out.println("WARNING: Can not close the test ByteArrayOutputStream for the standard output (out)! Cause: "+e.getMessage());
        } finally {
            out = null;
        }

        // Close the error stream:
        try {
            err.close();
        } catch (IOException e) {
            System.out.println("WARNING: Can not close the test ByteArrayOutputStream for the error output (err)! Cause: "+e.getMessage());
        } finally {
            err = null;
        }

        // Discard the used MarkdownReport:
        report = null;
    }

    @Test
    void setStatCollector() {
        // No stats by default:
        assertNull(report.validationStats);

        // Set some stats:
        report.setValidationStats(new StatCollector());
        assertNotNull(report.validationStats);

        // Set no stats:
        report.setValidationStats(null);
        assertNull(report.validationStats);
    }

    @Test
    void clear() {
        report.indexTest = 12;
        report.nbAvailableTests = 42;

        // Clear() should erase this information:
        report.clear();
        assertEquals(0, report.indexTest);
        assertEquals(0, report.nbAvailableTests);
    }

    private static String generateReportHeader(final String setName, final int nbAvailableTests){
        return "# ADQL Validation report" + LINE_SEP +
                LINE_SEP +
                LINE_SEP +
                "## About the validation set" + LINE_SEP +
                LINE_SEP +
                "| _Key_               | _Value_                                                      |" + LINE_SEP +
                "| ------------------- | ------------------------------------------------------------ |" + LINE_SEP +
                "| **Title**           | - |" + LINE_SEP +
                "| **Origin**          | " + setName + " |" + LINE_SEP +
                "| **Publisher**       | - |" + LINE_SEP +
                "| **Contact**         | - |" + LINE_SEP +
                "| **Description**     | - |" + LINE_SEP +
                "| **Available tests** | " + nbAvailableTests + " |" + LINE_SEP;
    }

    @Test
    void startWithNoSet() {
        // No validation set:
        report.start(null, null);

        // Check collected counts:
        assertEquals(0, report.nbAvailableTests);

        // Check the output:
        assertEquals(0, out.toByteArray().length);
        assertEquals(0, err.toByteArray().length);
    }

    @Test
    void startWithNoInformation() {
        // Create a really empty validation set:
        final ValidationSet validationSet = new ValidationSet();
        assertEquals(0, validationSet.queries.size());

        // Trigger the 'start' event:
        report.start(validationSet, null);

        // Check collected counts:
        assertEquals(validationSet.queries.size(), report.nbAvailableTests);

        // Check the output:
        assertEquals(generateReportHeader("-", 0), out.toString());
        assertEquals(0, err.toByteArray().length);
    }

    @Test
    void startWithAllInformation() throws MalformedURLException {
        // Create a validation set:
        final ValidationSet validationSet = new ValidationSet();
        // Set the set title:
        final String TITLE = "Super Original Validation Set Title";
        validationSet.title = TITLE;
        // Set all info about a publisher:
        validationSet.publisher = new Publisher();
        validationSet.publisher.name = "John Doe";
        final String PUBLISHER_EMAIL = "mailto:johndoe.org";
        validationSet.publisher.url = new URL(PUBLISHER_EMAIL);
        // Set all info about a contact person:
        validationSet.contact = new Contact();
        validationSet.contact.name = "Nobody";
        final String CONTACT_EMAIL = "mailto:nobody.org";
        validationSet.contact.url = new URL(CONTACT_EMAIL);
        // Set a (very smart) description:
        validationSet.description = "Here is a dummy description.";
        // Set at least one query:
        assertTrue(validationSet.queries.add(new ValidationQuery()));

        // Trigger the 'start' event:
        final String ORIGIN = "Out of thin air";
        report.start(validationSet, ORIGIN);

        // Check collected counts:
        assertEquals(validationSet.queries.size(), report.nbAvailableTests);

        // Check the output:
        assertEquals("# " + TITLE + LINE_SEP +
                LINE_SEP +
                LINE_SEP +
                "## About the validation set" + LINE_SEP +
                LINE_SEP +
                "| _Key_               | _Value_                                                      |" + LINE_SEP +
                "| ------------------- | ------------------------------------------------------------ |" + LINE_SEP +
                "| **Title**           | " + TITLE + " |" + LINE_SEP +
                "| **Origin**          | " + ORIGIN + " |" + LINE_SEP +
                "| **Publisher**       | [" + validationSet.publisher.name + "]("+PUBLISHER_EMAIL+")" + " |" + LINE_SEP +
                "| **Contact**         | [" + validationSet.contact.name + "]("+CONTACT_EMAIL+")" + " |" + LINE_SEP +
                "| **Description**     | " + validationSet.description + " |" + LINE_SEP +
                "| **Available tests** | " + validationSet.queries.size() + " |" + LINE_SEP, out.toString());
        assertEquals(0, err.toByteArray().length);
    }

    @Test
    void validatingNoQuery() {
        // Validating no query:
        report.validating(null);

        // The index of the current test should have been incremented:
        assertEquals(0, report.indexTest);

        // Check the output:
        assertEquals(0, out.toByteArray().length);
        assertEquals(0, err.toByteArray().length);
    }

    @Test
    void validatingWithQuery() {
        // Set useful counters:
        report.nbAvailableTests = 42;

        // Ensure the index of tests is null:
        assertEquals(0, report.indexTest);

        // Create a validation set:
        final ValidationSet vSet = new ValidationSet();
        final ValidationQuery QUERY = new ValidationQuery();
        vSet.queries.add(QUERY);

        // Trigger the 'validating' event:
        final String SET_NAME = "TEST";
        report.start(vSet, "TEST");
        report.validating(QUERY);

        // The index of the current test should have been incremented:
        assertEquals(1, report.indexTest);

        // Check the output (nothing about the query should yet be printed):
        assertEquals(generateReportHeader(SET_NAME, 1), out.toString());
        assertEquals(0, err.toByteArray().length);
    }

    @Test
    void passNoStats() {
        // Create a validation set:
        final ValidationSet vSet = new ValidationSet();
        final ValidationQuery QUERY = new ValidationQuery();
        vSet.queries.add(QUERY);

        // Trigger the 'pass' event:
        final String SET_NAME = "TEST";
        report.start(vSet, SET_NAME);
        report.validating(QUERY);
        report.pass(QUERY);

        // Check the output => Nothing except the header!
        assertEquals(generateReportHeader(SET_NAME, 1), out.toString());
        assertEquals(0, err.toByteArray().length);
    }

    @Test
    void passWithStats() {
        // Create useful stats:
        final ManualStats stats = new ManualStats();
        stats.lastTestDuration = (long)1e9;

        // Associate them with the TextReport:
        report.setValidationStats(stats);

        // Create a validation set:
        final ValidationSet vSet = new ValidationSet();
        final ValidationQuery QUERY = new ValidationQuery();
        vSet.queries.add(QUERY);

        // Trigger the 'pass' event:
        final String SET_NAME = "TEST";
        report.start(vSet, SET_NAME);
        report.validating(QUERY);
        report.pass(QUERY);

        // Check the output => Nothing except the header!
        assertEquals(generateReportHeader(SET_NAME, 1), out.toString());
        assertEquals(0, err.toByteArray().length);
    }

    @Test
    void failNoQuery() {
        // Trigger the 'fail' event with no query:
        report.fail(null, null);

        // Check the output => Nothing except the header!
        assertEquals(0, out.toByteArray().length);
        assertEquals(0, err.toByteArray().length);
    }

    @Test
    void failNoStats() {
        // Create a validation set:
        final ValidationSet vSet = new ValidationSet();
        final ValidationQuery QUERY = new ValidationQuery();
        QUERY.description = "Another dummy description.";
        QUERY.query = "SELECT something FROM somewhere";
        QUERY.isValid = true;
        vSet.queries.add(QUERY);

        // Trigger the 'fail' event:
        final String SET_NAME = "TEST";
        report.start(vSet, SET_NAME);
        report.validating(QUERY);
        final String ERROR_MSG = "Something is wrong somewhere!";
        report.fail(QUERY, ERROR_MSG);

        // Check the output => Nothing except the header!
        assertEquals(generateReportHeader(SET_NAME, 1), out.toString());
        assertEquals(0, err.toByteArray().length);
    }

    @Test
    void failWithStats() {
        // Create useful stats:
        final ManualStats stats = new ManualStats();
        stats.lastTestDuration = (long)1e9;

        // Associate them with the TextReport:
        report.setValidationStats(stats);

        // Create a validation set:
        final ValidationSet vSet = new ValidationSet();
        final ValidationQuery QUERY = new ValidationQuery();
        QUERY.description = null;             // No description!
        QUERY.query = null;                   // No query!
        QUERY.isValid = false;                // Expected to be not valid!
        vSet.queries.add(QUERY);

        // Trigger the 'pass' event:
        final String SET_NAME = "TEST";
        report.start(vSet, SET_NAME);
        report.validating(QUERY);
        report.fail(QUERY, null); // No error message!

        // Check the output => Nothing except the header!
        assertEquals(generateReportHeader(SET_NAME, 1), out.toString());
        assertEquals(0, err.toByteArray().length);
    }

    @Test
    void endNoSet() {
        // Trigger the 'end' event with no ValidationSet:
        report.end(null);

        // Check the output is empty:
        assertEquals(0, out.toByteArray().length);
        assertEquals(0, err.toByteArray().length);
    }

    @Test
    void endEmptySet() {
        // Trigger the 'end' event with a ValidationSet:
        final ValidationSet vSet = new ValidationSet();
        final String SET_NAME = "TEST";
        report.start(vSet, SET_NAME);
        report.end(vSet);

        // Check the output:
        assertEquals(generateReportHeader(SET_NAME, 0) +
                LINE_SEP +
                LINE_SEP +
                "## Summary" + LINE_SEP +
                LINE_SEP +
                "| GLOBAL RESULT             | OK |" + LINE_SEP +
                "| ------------------------- | ------------------------------------------------------------ |" + LINE_SEP +
                "| **Successful tests**      | 0 (100%) |" + LINE_SEP +
                "| **Total duration**        | - |" + LINE_SEP +
                "| **Average test duration** | - |" + LINE_SEP +
                "| **Execution date (UTC)**  | " + report.startDate + " |" + LINE_SEP, out.toString());
        assertEquals(0, err.toByteArray().length);
    }

    @Test
    void endNoStats() {
        // Create the validation set:
        final ValidationSet vSet = new ValidationSet();
        final String SET_NAME = "TEST";
        final ValidationQuery QUERY_1 = new ValidationQuery();
        QUERY_1.query = "SELECT something FROM some where a_stuff = another_stuff";
        QUERY_1.description = "Human blabla...";
        QUERY_1.adqlVersion = ADQLParser.ADQLVersion.V2_0;
        QUERY_1.isValid = false;
        vSet.queries.add(QUERY_1);
        final ValidationQuery QUERY_2 = new ValidationQuery();
        QUERY_2.query = "SELECT something weird";
        QUERY_2.description = null;
        QUERY_2.adqlVersion = ADQLParser.ADQLVersion.V2_1;
        QUERY_2.isValid = false;
        vSet.queries.add(QUERY_2);
        final ValidationQuery QUERY_3 = new ValidationQuery();
        QUERY_3.query = "Something is very strange here :/";
        QUERY_3.description = null;
        QUERY_3.adqlVersion = ADQLParser.ADQLVersion.V2_1;
        QUERY_3.isValid = true;
        vSet.queries.add(QUERY_3);

        // Simulate some validation queries:
        report.start(vSet, SET_NAME);
        report.validating(QUERY_1);
        report.pass(QUERY_1);
        report.validating(QUERY_2);
        report.pass(QUERY_2);
        report.validating(QUERY_3);
        report.fail(QUERY_3, "Missing everything ADQL needs!");

        // Finally, trigger the 'end' event with a ValidationSet:
        report.end(vSet);

        // Check the output:
        StringBuilder buf = new StringBuilder(generateReportHeader(SET_NAME, 3));
        buf.append(LINE_SEP)
           .append(LINE_SEP)
           .append("## Summary")
           .append(LINE_SEP)
           .append(LINE_SEP)
           .append("| GLOBAL RESULT             | 1 FAILED |")
           .append(LINE_SEP)
           .append("| ------------------------- | ------------------------------------------------------------ |")
           .append(LINE_SEP)
           .append("| **Successful tests**      | 2 (66%) |")
           .append(LINE_SEP)
           .append("| **Total duration**        | - |")
           .append(LINE_SEP)
           .append("| **Average test duration** | - |")
           .append(LINE_SEP)
           .append("| **Execution date (UTC)**  | ")
           .append(report.startDate)
           .append(" |")
           .append(LINE_SEP)
           .append(LINE_SEP)
           .append(LINE_SEP)
           .append("## Validation tests")
           .append(LINE_SEP)
           .append(LINE_SEP)
           .append("### Listing")
           .append(LINE_SEP)
           .append(LINE_SEP)
           .append("|    #    |                    UUID                    |    Status    |")
           .append(LINE_SEP)
           .append("| :-----: | :----------------------------------------: | :----------: |")
           .append(LINE_SEP)
           .append("| [3](#3) | [")
           .append(QUERY_3.id)
           .append("](#3) | [FAILED](#3) |")
           .append(LINE_SEP)
           .append("| [1](#1) | [")
           .append(QUERY_1.id)
           .append("](#1) | [OK](#1) |")
           .append(LINE_SEP)
           .append("| [2](#2) | [")
           .append(QUERY_2.id)
           .append("](#2) | [OK](#2) |")
           .append(LINE_SEP);

        int i = 0;
        for(ValidationQuery q : vSet.queries) {
            buf.append(LINE_SEP).append(LINE_SEP).append(LINE_SEP);
            buf.append("### ").append(++i).append(LINE_SEP).append(LINE_SEP);
            buf.append("* **UUID:** `").append(q.id).append('`').append(LINE_SEP).append(LINE_SEP);
            buf.append("* **Description:**").append(LINE_SEP).append(LINE_SEP);
            if (q.description != null)
                buf.append("  > ").append(q.description.replaceAll("" + LINE_SEP, "\n  >")).append(LINE_SEP).append(LINE_SEP);
            buf.append("* **Query:**").append(LINE_SEP).append(LINE_SEP);
            if (q.query != null)
                buf.append("  ```sql").append(LINE_SEP).append("  ").append(q.query.replaceAll("" + LINE_SEP, "\n  ")).append(LINE_SEP).append("  ```").append(LINE_SEP).append(LINE_SEP);
            buf.append("* **ADQL version:** ").append(q.adqlVersion).append(LINE_SEP).append(LINE_SEP);
            buf.append("* **Expected:** ").append(q.isValid ? "success" : "failure").append(LINE_SEP).append(LINE_SEP);
            buf.append("* **Test:**").append(LINE_SEP).append(LINE_SEP);
            buf.append("  * **Duration:** ").append(report.testedQueries.get(i-1).duration < 0 ? "-" : report.testedQueries.get(i-1).duration).append(LINE_SEP).append(LINE_SEP);
            buf.append("  * **Status:** ").append(report.testedQueries.get(i-1).passed ? "OK" : "FAILED").append(LINE_SEP);
            if (report.testedQueries.get(i-1).error != null) {
                buf.append(LINE_SEP).append("  * **Error:**").append(LINE_SEP).append(LINE_SEP);
                buf.append("    > ").append(report.testedQueries.get(i-1).error.replaceAll("" + LINE_SEP, "\n    > ")).append(LINE_SEP);
            }
        }

        assertEquals(buf.toString(), out.toString());
        assertEquals(0, err.toByteArray().length);
    }

    @Test
    void endWithStats() {
        // Create the validation set:
        final ValidationSet vSet = new ValidationSet();
        final String SET_NAME = "TEST";
        final ValidationQuery QUERY_1 = new ValidationQuery();
        QUERY_1.query = "SELECT something FROM some where a_stuff = another_stuff";
        QUERY_1.description = "Human blabla...";
        QUERY_1.adqlVersion = ADQLParser.ADQLVersion.V2_0;
        QUERY_1.isValid = false;
        vSet.queries.add(QUERY_1);
        final ValidationQuery QUERY_2 = new ValidationQuery();
        QUERY_2.query = "SELECT something weird";
        QUERY_2.description = null;
        QUERY_2.adqlVersion = ADQLParser.ADQLVersion.V2_1;
        QUERY_2.isValid = false;
        vSet.queries.add(QUERY_2);
        final ValidationQuery QUERY_3 = new ValidationQuery();
        QUERY_3.query = "Something is very strange here :/";
        QUERY_3.description = null;
        QUERY_3.adqlVersion = ADQLParser.ADQLVersion.V2_1;
        QUERY_3.isValid = true;
        vSet.queries.add(QUERY_3);

        // Create useful stats:
        final ManualStats stats = new ManualStats();
        stats.totalDuration = (long)2e9;
        stats.cntTests = vSet.queries.size();
        stats.cntPass = 2;

        // Associate them with the TextReport:
        report.setValidationStats(stats);

        // Simulate some validation queries:
        report.start(vSet, SET_NAME);
        report.validating(QUERY_1);
        report.pass(QUERY_1);
        report.validating(QUERY_2);
        report.pass(QUERY_2);
        report.validating(QUERY_3);
        report.fail(QUERY_3, "Missing everything ADQL needs!");

        // Finally, trigger the 'end' event with a ValidationSet:
        report.end(vSet);

        // Check the output:
        StringBuilder buf = new StringBuilder(generateReportHeader(SET_NAME, 3));
        buf.append(LINE_SEP)
           .append(LINE_SEP)
           .append("## Summary")
           .append(LINE_SEP)
           .append(LINE_SEP)
           .append("| GLOBAL RESULT             | 1 FAILED |")
           .append(LINE_SEP)
           .append("| ------------------------- | ------------------------------------------------------------ |")
           .append(LINE_SEP)
           .append("| **Successful tests**      | 2 (66%) |")
           .append(LINE_SEP)
           .append("| **Total duration**        | 2.0s |")
           .append(LINE_SEP)
           .append("| **Average test duration** | 666.6667ms |")
           .append(LINE_SEP)
           .append("| **Execution date (UTC)**  | ")
           .append(report.startDate)
           .append(" |")
           .append(LINE_SEP)
           .append(LINE_SEP)
           .append(LINE_SEP)
           .append("## Validation tests")
           .append(LINE_SEP)
           .append(LINE_SEP)
           .append("### Listing")
           .append(LINE_SEP)
           .append(LINE_SEP)
           .append("|    #    |                    UUID                    |    Status    |")
           .append(LINE_SEP)
           .append("| :-----: | :----------------------------------------: | :----------: |")
           .append(LINE_SEP)
           .append("| [3](#3) | [")
           .append(QUERY_3.id)
           .append("](#3) | [FAILED](#3) |")
           .append(LINE_SEP)
           .append("| [1](#1) | [")
           .append(QUERY_1.id)
           .append("](#1) | [OK](#1) |")
           .append(LINE_SEP)
           .append("| [2](#2) | [")
           .append(QUERY_2.id)
           .append("](#2) | [OK](#2) |")
           .append(LINE_SEP);

        int i = 0;
        for(ValidationQuery q : vSet.queries) {
            buf.append(LINE_SEP).append(LINE_SEP).append(LINE_SEP);
            buf.append("### ").append(++i).append(LINE_SEP).append(LINE_SEP);
            buf.append("* **UUID:** `").append(q.id).append('`').append(LINE_SEP).append(LINE_SEP);
            buf.append("* **Description:**").append(LINE_SEP).append(LINE_SEP);
            if (q.description != null)
                buf.append("  > ").append(q.description.replaceAll("" + LINE_SEP, "\n  >")).append(LINE_SEP).append(LINE_SEP);
            buf.append("* **Query:**").append(LINE_SEP).append(LINE_SEP);
            if (q.query != null)
                buf.append("  ```sql").append(LINE_SEP).append("  ").append(q.query.replaceAll("" + LINE_SEP, "\n  ")).append(LINE_SEP).append("  ```").append(LINE_SEP).append(LINE_SEP);
            buf.append("* **ADQL version:** ").append(q.adqlVersion).append(LINE_SEP).append(LINE_SEP);
            buf.append("* **Expected:** ").append(q.isValid ? "success" : "failure").append(LINE_SEP).append(LINE_SEP);
            buf.append("* **Test:**").append(LINE_SEP).append(LINE_SEP);
            buf.append("  * **Duration:** ").append(report.testedQueries.get(i-1).duration < 0 ? "-" : report.testedQueries.get(i-1).duration + ".0ms").append(LINE_SEP).append(LINE_SEP);
            buf.append("  * **Status:** ").append(report.testedQueries.get(i-1).passed ? "OK" : "FAILED").append(LINE_SEP);
            if (report.testedQueries.get(i-1).error != null) {
                buf.append(LINE_SEP).append("  * **Error:**").append(LINE_SEP).append(LINE_SEP);
                buf.append("    > ").append(report.testedQueries.get(i-1).error.replaceAll("" + LINE_SEP, "\n    > ")).append(LINE_SEP);
            }
        }

        assertEquals(buf.toString(), out.toString());
        assertEquals(0, err.toByteArray().length);
    }

    @Test
    void errorNoException() {
        // Trigger the 'error' event without an Exception:
        report.error(null);
        // => no error output!
        assertEquals(0, out.toByteArray().length);
        assertEquals(0, err.toByteArray().length);
    }

    @Test
    void errorWithException() {
        // Trigger the 'error' event with an Exception:
        final String ERROR_MSG = "Oops!";

        // Trigger the 'end' event with a ValidationSet:
        final ValidationSet vSet = new ValidationSet();
        final String SET_NAME = "TEST";
        report.start(vSet, SET_NAME);
        report.error(new ValidationException(ERROR_MSG));
        report.end(vSet);

        // Check the output:
        assertEquals(generateReportHeader(SET_NAME, 0) +
                LINE_SEP +
                LINE_SEP +
                "## Summary" + LINE_SEP +
                "" + LINE_SEP +
                "| GLOBAL RESULT             | OK |" + LINE_SEP +
                "| ------------------------- | ------------------------------------------------------------ |" + LINE_SEP +
                "| **Successful tests**      | 0 (100%) |" + LINE_SEP +
                "| **Total duration**        | - |" + LINE_SEP +
                "| **Average test duration** | - |" + LINE_SEP +
                "| **Execution date (UTC)**  | " + report.startDate + " |" + LINE_SEP +
                LINE_SEP +
                LINE_SEP +
                LINE_SEP +
                "## General errors" + LINE_SEP +
                "" + LINE_SEP +
                "* Error #1:" + LINE_SEP +
                "  > Oops!" + LINE_SEP, out.toString());
        assertEquals(0, err.toByteArray().length);
    }

    @Test
    void errorWithExceptionButNoMessage() {
        // Trigger the 'error' event with an Exception:
        final ValidationSet vSet = new ValidationSet();
        final String SET_NAME = "TEST";
        report.start(vSet, SET_NAME);
        report.error(new ValidationException());
        report.end(vSet);

        // Check the output:
        assertEquals(generateReportHeader(SET_NAME, 0) +
                LINE_SEP +
                LINE_SEP +
                "## Summary" + LINE_SEP +
                "" + LINE_SEP +
                "| GLOBAL RESULT             | OK |" + LINE_SEP +
                "| ------------------------- | ------------------------------------------------------------ |" + LINE_SEP +
                "| **Successful tests**      | 0 (100%) |" + LINE_SEP +
                "| **Total duration**        | - |" + LINE_SEP +
                "| **Average test duration** | - |" + LINE_SEP +
                "| **Execution date (UTC)**  | " + report.startDate + " |" + LINE_SEP +
                LINE_SEP +
                LINE_SEP +
                LINE_SEP +
                "## General errors" + LINE_SEP +
                "" + LINE_SEP +
                "* Error #1:" + LINE_SEP +
                "  > <cds.adql.validation.ValidationException>" + LINE_SEP, out.toString());
        assertEquals(0, err.toByteArray().length);
    }

    @Test
    void formatContactStringString_WithNoNameButNormalURL() throws Exception {
        final URL url = new URL("https://ivoa.net");
        assertEquals("<"+url+">", report.formatContact(null, url));
        assertEquals("<"+url+">", report.formatContact("", url));
        assertEquals("<"+url+">", report.formatContact("       ", url));
    }

    @Test
    void formatContactStringString_WithNoNameButMailto() throws Exception {
        final String email = "me@ivoa.net";
        final URL url = new URL("mailto:"+email);
        final String EXPECTED = "["+email+"]("+url+")";
        assertEquals(EXPECTED, report.formatContact(null, url));
        assertEquals(EXPECTED, report.formatContact("", url));
        assertEquals(EXPECTED, report.formatContact("       ", url));
    }

    @Test
    void formatContactStringString_WithNoURL() {
        final String name = "My Name";
        assertEquals(name, report.formatContact("\t"+name+"  ", null));
    }

    @Test
    void formatContactStringString_WithNoNameAndNoURL() {
        assertEquals("-", report.formatContact(null, null));
        assertEquals("-", report.formatContact("", null));
        assertEquals("-", report.formatContact("       ", null));
    }

    @Test
    void formatContactStringString_WithNameAndNormalURL() throws Exception {
        final String name = "My Name";
        final URL url = new URL("https://ivoa.net");
        assertEquals("["+name+"]("+url+")", report.formatContact(name, url));
    }

    @Test
    void formatContactStringString_WithNameAndMailto() throws Exception {
        final String name = "My Name";
        final URL url = new URL("mailto:me@ivoa.net");
        assertEquals("["+name+"]("+url+")", report.formatContact(name, url));
    }
}