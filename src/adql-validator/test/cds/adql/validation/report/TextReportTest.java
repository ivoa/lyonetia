package cds.adql.validation.report;

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

class TextReportTest {

    private final static String LINE_SEP = System.lineSeparator();

    private TextReport report = null;
    private ByteArrayOutputStream out = null;
    private ByteArrayOutputStream err = null;

    @BeforeEach
    void setUp() {
        out = new ByteArrayOutputStream();
        err = new ByteArrayOutputStream();
        report = new TextReport(new PrintStream(out), new PrintStream(err));
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

        // Discard the used TextReport:
        report = null;
    }

    private static String generateReportHeader(final String setName, final int nbAvailableTests){
        return "Starting validation..." + LINE_SEP +
                TextReport.SEPARATOR + LINE_SEP +
                "Origin         : " + setName + LINE_SEP +
                "Publisher      : -" + LINE_SEP +
                "Contact        : -" + LINE_SEP +
                "Description    : -" + LINE_SEP +
                "Available tests: " + nbAvailableTests + LINE_SEP +
                TextReport.SEPARATOR + LINE_SEP;
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
        // Set all info about a publisher:
        validationSet.publisher = new Publisher();
        validationSet.publisher.name = "John Doe";
        final String PUBLISHER_EMAIL = "johndoe.org";
        validationSet.publisher.url = new URL("mailto:"+PUBLISHER_EMAIL);
        // Set all info about a contact person:
        validationSet.contact = new Contact();
        validationSet.contact.name = "Nobody";
        final String CONTACT_EMAIL = "nobody.org";
        validationSet.contact.url = new URL("mailto:"+CONTACT_EMAIL);
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
        assertEquals("Starting validation..." + LINE_SEP +
                TextReport.SEPARATOR + LINE_SEP +
                "Origin         : " + ORIGIN + LINE_SEP +
                "Publisher      : " + validationSet.publisher.name + " ("+PUBLISHER_EMAIL+")" + LINE_SEP +
                "Contact        : " + validationSet.contact.name + " ("+CONTACT_EMAIL+")" + LINE_SEP +
                "Description    : " + validationSet.description + LINE_SEP +
                "Available tests: " + validationSet.queries.size() + LINE_SEP +
                TextReport.SEPARATOR + LINE_SEP, out.toString());
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
    void passNoQuery() {
        // Trigger the 'pass' event with no query:
        report.validating(null);

        // Check the output:
        assertEquals(0, out.toByteArray().length);
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

        // Check the output:
        assertEquals(generateReportHeader(SET_NAME, 1) +
                "1/1 - "+QUERY.id+" - SUCCESS" + LINE_SEP +
                TextReport.LIGHT_SEP + LINE_SEP +
                "Description: -" + LINE_SEP +
                "Query      : "   + LINE_SEP +
                "Expected   : failure" + LINE_SEP +
                TextReport.SEPARATOR + LINE_SEP, out.toString());
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

        // Check the output:
        assertEquals(generateReportHeader(SET_NAME, 1) +
                "1/1 - "+QUERY.id+" - SUCCESS" + LINE_SEP +
                "Executed in 1000.0ms" + LINE_SEP +
                TextReport.LIGHT_SEP + LINE_SEP +
                "Description: -" + LINE_SEP +
                "Query      : "   + LINE_SEP +
                "Expected   : failure" + LINE_SEP +
                TextReport.SEPARATOR + LINE_SEP, out.toString());
        assertEquals(0, err.toByteArray().length);
    }

    @Test
    void failNoQuery() {
        // Trigger the 'fail' event with no query:
        report.fail(null, null);

        // Check the output:
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

        // Check the output:
        assertEquals(generateReportHeader(SET_NAME, 1) +
                "1/1 - "+QUERY.id+" - FAILED" + LINE_SEP +
                "Due to: a parsing error: " + ERROR_MSG + LINE_SEP +
                TextReport.LIGHT_SEP + LINE_SEP +
                "Description: " + QUERY.description + LINE_SEP +
                "Query      : " + QUERY.query + LINE_SEP +
                "Expected   : success" + LINE_SEP +
                TextReport.SEPARATOR + LINE_SEP, out.toString());
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

        // Check the output:
        assertEquals(generateReportHeader(SET_NAME, 1) +
                "1/1 - "+QUERY.id+" - FAILED" + LINE_SEP +
                "Due to: an unexpected result" + LINE_SEP +
                "Executed in 1000.0ms" + LINE_SEP +
                TextReport.LIGHT_SEP + LINE_SEP +
                "Description: -" + LINE_SEP +
                "Query      : " + LINE_SEP +
                "Expected   : failure" + LINE_SEP +
                TextReport.SEPARATOR + LINE_SEP, out.toString());
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
    void endNoStats() {
        // Trigger the 'end' event with a ValidationSet:
        report.end(new ValidationSet());

        // Check the output:
        assertEquals("Validation completed!" + LINE_SEP +
                TextReport.LIGHT_SEP + LINE_SEP +
                "Summary : 0/0 successful tests (100%)" + LINE_SEP +
                TextReport.SEPARATOR + LINE_SEP, out.toString());
        assertEquals(0, err.toByteArray().length);
    }

    @Test
    void endWithStats() {
        // Artificially set the number of tests:
        report.nbPassed = 21;
        report.nbAvailableTests = 42;

        // Create useful stats:
        final ManualStats stats = new ManualStats();
        stats.cntPass = report.nbPassed;
        stats.cntTests = report.nbAvailableTests;
        stats.totalDuration = (long)1e10;

        // Associate them with the TextReport:
        report.setValidationStats(stats);

        // Trigger the 'end' event with a ValidationSet:
        report.end(new ValidationSet());

        // Check the output:
        assertEquals("Validation completed!" + LINE_SEP +
                TextReport.LIGHT_SEP + LINE_SEP +
                "Summary : " + report.nbPassed + "/" + report.nbAvailableTests + " successful tests (50%)" + LINE_SEP +
                "Duration: total=10.0s, average=238.09523ms" + LINE_SEP +
                TextReport.SEPARATOR + LINE_SEP, out.toString());
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
        report.error(new ValidationException(ERROR_MSG));

        // Check the output:
        assertEquals(0, out.toByteArray().length);
        assertEquals("ERROR: " + ERROR_MSG + LINE_SEP, err.toString());
    }

    @Test
    void errorWithExceptionButNoMessage() {
        // Trigger the 'error' event with an Exception:
        report.error(new ValidationException());

        // Check the output:
        assertEquals(0, out.toByteArray().length);
        assertEquals("ERROR: <cds.adql.validation.ValidationException>" + LINE_SEP, err.toString());
    }
}