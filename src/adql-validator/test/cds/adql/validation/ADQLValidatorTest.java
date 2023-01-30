package cds.adql.validation;

import adql.parser.ADQLParser;
import cds.adql.validation.parser.ValidationSetParser;
import cds.adql.validation.query.ValidationQuery;
import cds.adql.validation.query.ValidationSet;
import cds.adql.validation.report.RecorderListener;
import cds.adql.validation.report.StatCollector;
import cds.adql.validation.report.ValidatorListener;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.Iterator;
import java.util.UUID;

import static adql.parser.ADQLParser.ADQLVersion;
import static org.junit.jupiter.api.Assertions.*;

class ADQLValidatorTest {

    /* *************************************************************************
     * PARSERS MANAGEMENT
     */

    @Test
    void getParser_Null()
    {
        // Get a parser:
        final ADQLParser parser = (new ADQLValidator()).getParser(null);

        // A parser with the default ADQL version MUST be returned:
        assertNotNull(parser);
        assertEquals(ValidationSetParser.DEFAULT_ADQL_VERSION, parser.getADQLVersion());

        // No restriction on the coordinate system:
        assertNull(parser.getAllowedCoordSys());
    }

    @Test
    void getParser_AnyVersion()
    {
        // Try all possible versions:
        for(ADQLVersion version : ADQLVersion.values()){
            // Get a parser:
            final ADQLParser parser = (new ADQLValidator()).getParser(version);

            // A parser with the specified ADQL version MUST be returned:
            assertNotNull(parser);
            assertEquals(version, parser.getADQLVersion());

            // No restriction on the coordinate system:
            assertNull(parser.getAllowedCoordSys());
        }
    }

    /* *************************************************************************
     * LISTENERS MANAGEMENT
     */

    @Test
    void addListener_Null(){
        assertFalse((new ADQLValidator()).addListener(null));
    }

    @Test
    void addListener_NotNull(){
        final ADQLValidator validator = new ADQLValidator();
        final ValidatorListener listener = new StatCollector();

        // Ensure a listener can be added:
        assertTrue(validator.addListener(listener));

        // Ensure it has been effectively added:
        Iterator<ValidatorListener> itListener = validator.getListeners();
        assertTrue(itListener.hasNext());
        assertEquals(listener, itListener.next());
        assertFalse(itListener.hasNext());

        // Ensure it can not be added again:
        assertTrue(validator.addListener(listener));
        itListener = validator.getListeners();
        assertTrue(itListener.hasNext());
        assertEquals(listener, itListener.next());
        assertFalse(itListener.hasNext());

    }

    @Test
    void addListener_Integer_Null(){
        assertFalse((new ADQLValidator()).addListener(0, null));
    }

    @Test
    void addListener_Integer_NotNull(){
        final ADQLValidator validator = new ADQLValidator();
        final ValidatorListener listener = new StatCollector();

        // Ensure a listener can be added:
        assertTrue(validator.addListener(0, listener));

        // Ensure it has been effectively added:
        Iterator<ValidatorListener> itListener = validator.getListeners();
        assertTrue(itListener.hasNext());
        assertEquals(listener, itListener.next());
        assertFalse(itListener.hasNext());

        // Ensure it can not be added again:
        assertTrue(validator.addListener(1, listener));
        itListener = validator.getListeners();
        assertTrue(itListener.hasNext());
        assertEquals(listener, itListener.next());
        assertFalse(itListener.hasNext());
    }

    @Test
    void addListener_Incorrect_Index(){
        final ADQLValidator validator = new ADQLValidator();
        final ValidatorListener listener = new StatCollector();

        // Ensure a listener can not be added if the index is incorrect:
        assertFalse(validator.addListener(3, listener));
    }

    @Test
    void removeListener_Null(){
        assertFalse((new ADQLValidator()).removeListener(null));
    }

    @Test
    void removeListener_NotExisting(){
        assertFalse((new ADQLValidator()).removeListener(new StatCollector()));
    }

    @Test
    void removeListener_Existing(){
        final ADQLValidator validator = new ADQLValidator();
        final ValidatorListener listener = new StatCollector();

        // Add the listener:
        assertTrue(validator.addListener(listener));

        // Try removing this listener:
        assertTrue(validator.removeListener(listener));

        // Ensure it does no longer exist:
        assertFalse(validator.getListeners().hasNext());
    }

    @Test
    void removeListener_BadIndex(){
        assertNull((new ADQLValidator()).removeListener(3));
    }

    @Test
    void removeListener_CorrectIndex(){
        final ADQLValidator validator = new ADQLValidator();
        final ValidatorListener listener = new StatCollector();

        // Add the listener:
        assertTrue(validator.addListener(listener));

        // Try removing this listener:
        assertEquals(listener, validator.removeListener(0));

        // Ensure it does no longer exist:
        assertFalse(validator.getListeners().hasNext());
    }

    /* *************************************************************************
     * GENERIC VALIDATION
     */

    @Test
    void validate_Set_Null() {
        assertFalse((new ADQLValidator()).validate(null, (String)null));
    }

    @Test
    void validate_Set_Empty() {
        assertTrue((new ADQLValidator()).validate(new ValidationSet(), null));
    }

    @Test
    void validate_Set_AllValid() {
        // Create a validation set:
        final ValidationSet querySet = new ValidationSet();

        // Create a useful listener:
        final ADQLValidator validator = new ADQLValidator();
        final RecorderListener stats = new RecorderListener();
        validator.addListener(stats);
        
        // Create 2 valid queries:
        // 1
        ValidationQuery query = new ValidationQuery(UUID.randomUUID());
        query.isValid = true;
        query.query = "SELECT x FROM y";
        querySet.queries.add(query);
        // 2
        query = new ValidationQuery(UUID.randomUUID());
        query.isValid = false;
        query.query = "SELECT everything";
        querySet.queries.add(query);

        // Check the initial state:
        assertFalse(stats.isSetStarted());
        assertFalse(stats.isSetEnded());
        assertFalse(stats.isQueryStarted());
        assertFalse(stats.isQueryEnded());
        assertEquals(0, stats.getCntPass());
        assertEquals(0, stats.getCntFail());

        // Try to validate the set and ensure it succeeds:
        assertTrue(validator.validate(querySet, null));
        assertTrue(stats.isSetStarted());
        assertTrue(stats.isSetEnded());
        assertTrue(stats.isQueryStarted());
        assertTrue(stats.isQueryEnded());
        assertEquals(2, stats.getCntPass());
        assertEquals(0, stats.getCntFail());
    }

    @Test
    void validate_Set_OneFailed() {
        // Create a validation set:
        final ValidationSet querySet = new ValidationSet();

        // Create useful listeners:
        final ADQLValidator validator = new ADQLValidator();
        final RecorderListener stats = new RecorderListener();
        validator.addListener(stats);

        // Create 2 valid queries:
        // 1
        ValidationQuery query = new ValidationQuery(UUID.randomUUID());
        query.isValid = false; // [IT SHOULD BE true (as in validate_Set_AllValid())]
        query.query = "SELECT x FROM y";
        querySet.queries.add(query);
        // 2
        query = new ValidationQuery(UUID.randomUUID());
        query.isValid = false;
        query.query = "SELECT everything";
        querySet.queries.add(query);

        // Check the initial state:
        assertFalse(stats.isSetStarted());
        assertFalse(stats.isSetEnded());
        assertFalse(stats.isQueryStarted());
        assertFalse(stats.isQueryEnded());
        assertEquals(0, stats.getCntPass());
        assertEquals(0, stats.getCntFail());

        // Try to validate the set and ensure it succeeds:
        assertFalse(validator.validate(querySet, null));
        assertTrue(stats.isSetStarted());
        assertTrue(stats.isSetEnded());
        assertTrue(stats.isQueryStarted());
        assertTrue(stats.isQueryEnded());
        assertEquals(1, stats.getCntPass());
        assertEquals(1, stats.getCntFail());
    }

    @Test
    void validate_Query_Null() {
        assertFalse((new ADQLValidator()).validate(null));
    }

    @Test
    void validate_Query_EmptyQueryString() {
        assertFalse((new ADQLValidator()).validate(new ValidationQuery()));
    }

    @Test
    void validate_Query_ValidWithTrueExpected() {
        final ADQLValidator validator = new ADQLValidator();

        // Create useful listeners:
        final RecorderListener stats = new RecorderListener();
        validator.addListener(stats);

        // Create a valid query:
        final ValidationQuery query = new ValidationQuery(UUID.randomUUID());
        query.isValid = true;
        query.query = "SELECT x FROM y";

        // Check the initial state:
        assertFalse(stats.isQueryStarted());
        assertFalse(stats.isQueryEnded());
        assertEquals(0, stats.getCntPass());
        assertEquals(0, stats.getCntFail());

        // Try to validate the set and ensure it succeeds:
        assertTrue(validator.validate(query));
        assertTrue(stats.isQueryStarted());
        assertTrue(stats.isQueryEnded());
        assertEquals(1, stats.getCntPass());
        assertEquals(0, stats.getCntFail());
    }

    @Test
    void validate_Query_ValidWithFalseExpected() {
        final ADQLValidator validator = new ADQLValidator();

        // Create useful listeners:
        final RecorderListener stats = new RecorderListener();
        validator.addListener(stats);

        // Create a valid query:
        final ValidationQuery query = new ValidationQuery(UUID.randomUUID());
        query.isValid = false;
        query.query = "SELECT everything";

        // Check the initial state:
        assertFalse(stats.isQueryStarted());
        assertFalse(stats.isQueryEnded());
        assertEquals(0, stats.getCntPass());
        assertEquals(0, stats.getCntFail());

        // Try to validate the set and ensure it succeeds:
        assertTrue(validator.validate(query));
        assertTrue(stats.isQueryStarted());
        assertTrue(stats.isQueryEnded());
        assertEquals(1, stats.getCntPass());
        assertEquals(0, stats.getCntFail());
    }

    @Test
    void validate_Query_InvalidWithTrueExpected() {
        final ADQLValidator validator = new ADQLValidator();

        // Create useful listeners:
        final RecorderListener stats = new RecorderListener();
        validator.addListener(stats);

        // Create a valid query:
        final ValidationQuery query = new ValidationQuery(UUID.randomUUID());
        query.isValid = true;
        query.query = "SELECT everything";

        // Check the initial state:
        assertFalse(stats.isQueryStarted());
        assertFalse(stats.isQueryEnded());
        assertEquals(0, stats.getCntPass());
        assertEquals(0, stats.getCntFail());

        // Try to validate the set and ensure it succeeds:
        assertFalse(validator.validate(query));
        assertTrue(stats.isQueryStarted());
        assertTrue(stats.isQueryEnded());
        assertEquals(0, stats.getCntPass());
        assertEquals(1, stats.getCntFail());
    }

    @Test
    void validate_Query_InvalidWithFalseExpected() {
        final ADQLValidator validator = new ADQLValidator();

        // Create useful listeners:
        final RecorderListener stats = new RecorderListener();
        validator.addListener(stats);

        // Create a valid query:
        final ValidationQuery query = new ValidationQuery(UUID.randomUUID());
        query.isValid = false;
        query.query = "SELECT x FROM y";

        // Check the initial state:
        assertFalse(stats.isQueryStarted());
        assertFalse(stats.isQueryEnded());
        assertEquals(0, stats.getCntPass());
        assertEquals(0, stats.getCntFail());

        // Try to validate the set and ensure it succeeds:
        assertFalse(validator.validate(query));
        assertTrue(stats.isQueryStarted());
        assertTrue(stats.isQueryEnded());
        assertEquals(0, stats.getCntPass());
        assertEquals(1, stats.getCntFail());
    }

    /* *************************************************************************
     * XML VALIDATION
     */

    @Test
    void checkXML_Null() {
        assertThrows(NullPointerException.class,
                ()->(new ADQLValidator()).checkXML(null),
                "Missing input validation set!");
    }

    /*
     * No need to test more checkXML(InputStream) here ; it is using the class
     * XMLValidationSetParser already very well tested.
     */

    @Test
    void validateXML_Null() {
        assertThrows(NullPointerException.class,
                ()->(new ADQLValidator()).validateXML(null, null),
                "Missing input validation set!");
    }

    @Test
    void validateXML_EmptyValidationSet() {
        final ADQLValidator validator = new ADQLValidator();

        // Trace all errors:
        final RecorderListener listener = new RecorderListener();
        validator.addListener(listener);

        // Try validating an incorrect XML document and ensure it fails:
        assertTrue(validator.validateXML(new ByteArrayInputStream("<queries></queries>".getBytes()), null));

        // No error and failure message should have been published to listeners:
        assertEquals(0, listener.getErrors().length());
        assertEquals(0, listener.getFailures().length());
    }

    @Test
    void validateXML_IncorrectXML() {
        final ADQLValidator validator = new ADQLValidator();

        // Trace all errors:
        final RecorderListener listener = new RecorderListener();
        validator.addListener(listener);

        // Try validating an incorrect XML document and ensure it fails:
        assertFalse(validator.validateXML(new ByteArrayInputStream("<hello>World</hello>".getBytes()), null));

        // An error message should have been published to listeners:
        assertEquals("XML document parsing failed! Cause: org.xml.sax.SAXParseException; lineNumber: 1; columnNumber: 8; Unsupported XML root tag: <hello>! Expected: <queries>.", listener.getErrors());
        assertEquals(0, listener.getFailures().length());
    }

    @Test
    void validateXML_CorrectXMLAndQueries() {
        final ADQLValidator validator = new ADQLValidator();

        // Trace all errors:
        final RecorderListener listener = new RecorderListener();
        validator.addListener(listener);

        // Try validating a valid validation set and ensure it succeeds:
        assertTrue(validator.validateXML(new ByteArrayInputStream("<queries><description>Some description for the entire set.</description><query uuid=\"ccd99070-4508-11e6-b60c-9d2c33f9b7a2\"><description>The simplest ADQL query.</description><adql valid=\"true\" version=\"adql-2.0\">select x from y</adql></query></queries>".getBytes()), "Inline test"));

        // No error message should have been published to listeners:
        assertEquals(0, listener.getErrors().length());
        assertEquals(0, listener.getFailures().length());
    }

    @Test
    void validateXML_CorrectXML_ButIncorrectQuery() {
        final ADQLValidator validator = new ADQLValidator();

        // Trace all errors:
        final RecorderListener listener = new RecorderListener();
        validator.addListener(listener);

        // Try validating a correct XML, but wrong query and ensure it fails:
        assertFalse(validator.validateXML(new ByteArrayInputStream("<queries><description>Some description for the entire set.</description><query uuid=\"ccd99070-4508-11e6-b60c-9d2c33f9b7a2\"><description>Wrong ADQL query.</description><adql valid=\"true\" version=\"adql-2.0\">select everything</adql></query></queries>".getBytes()), "Inline test"));

        // A failure message should have been published to listeners:
        assertEquals(0, listener.getErrors().length());
        assertEquals(" Encountered \"<EOF>\". Was expecting one of: \".\" \",\" \"FROM\" \"AS\" \"\\\"\" <REGULAR_IDENTIFIER_CANDIDATE> \".\" \".\" \".\" \".\" \".\" \".\" \".\" \".\" \".\" \".\" \".\" \".\" \".\" \".\" \".\" \".\" \".\" \".\" \".\" \".\" \".\" \".\" \".\" \".\" \".\" \".\" \".\" \".\" \".\" \".\" \".\" \".\" \".\" \".\" \".\" \".\" \".\" \".\" ", listener.getFailures());
    }
}