package cds.adql.validation;

import adql.parser.ADQLParser;
import adql.parser.grammar.ParseException;
import cds.adql.validation.parser.ValidationSetParser;
import cds.adql.validation.parser.xml.XMLValidationSetParser;
import cds.adql.validation.query.UDF;
import cds.adql.validation.query.ValidationQuery;
import cds.adql.validation.query.ValidationSet;
import cds.adql.validation.report.ValidatorListener;

import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static adql.parser.ADQLParser.ADQLVersion;

/**
 * Validator tool for queries set and individual queries, whatever is their
 * format (XML or Java Object).
 *
 * <p>
 *     This validator can deal with all supported ADQL versions.
 *     <em>See {@link #getParser(ADQLVersion)}.</em>
 * </p>
 *
 * <p>
 *     Any validation process reports its progress and result to all registered
 *     {@link ValidatorListener}. Some are already provided (
 *     {@link cds.adql.validation.report.StatCollector},
 *     {@link cds.adql.validation.report.MarkdownReport} and
 *     {@link cds.adql.validation.report.TextReport}) but custom ones are
 *     encouraged in case of special needs.
 * </p>
 *
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 1.0 (12/2021)
 */
public class ADQLValidator {

    final static Logger LOGGER = Logger.getLogger(ADQLValidator.class.getName());

    private final List<ValidatorListener> listeners;

    private final Map<ADQLVersion, ADQLParser> parsers;

    /* ********************************************************************** */

    public ADQLValidator(){
        listeners = new ArrayList<>();
        parsers = new HashMap<>(ADQLVersion.values().length);
    }

    /* *************************************************************************
     * LISTENERS MANAGEMENT
     */

    /**
     * Add a listener to this validator.
     *
     * <p>
     *  This function rejects NULL listeners ; it returns <code>false</code>
     *  immediately.
     * </p>
     *
     * <p>
     *  This function does nothing more than returning <code>true</code>
     *  immediately if the given listener is already registered with this
     *  validator.
     * </p>
     *
     * <p><i><b>Note:</b>
     *  If the listener can not be added, this function returns
     *  <code>false</code> and logs the reason as a WARNING message.
     * </i></p>
     *
     * @param listener  The new listener.
     *
     * @return <code>true</code> if the listener has been added,
     *         <code>false</code> otherwise (e.g. if already listening).
     */
    public final boolean addListener(final ValidatorListener listener){
        // Forbid NULL listeners:
        if (listener == null)
            return false;

        // Do nothing more if already registered:
        else if (listeners.contains(listener))
            return true;

        // Otherwise, use the standard ArrayList `add(Object) function:
        try{
            return listeners.add(listener);
        }

        // ...but in case of error, log a WARNING and return `false`:
        catch(Exception ex){
            LOGGER.log(Level.WARNING, "Impossible to add the given listener! Cause: "+ex.getMessage(), ex);
            return false;
        }
    }

    /**
     * Insert the given listener at the given position in the listeners list.
     *
     * <p>
     *  This function rejects NULL listeners ; it returns <code>false</code>
     *  immediately.
     * </p>
     *
     * <p>
     *  This function does nothing more than returning <code>true</code>
     *  immediately if the given listener is already registered with this
     *  validator.
     * </p>
     *
     * <p><i><b>Note:</b>
     *  If the listener can not be added, this function returns
     *  <code>false</code> and logs the reason as a WARNING message.
     * </i></p>
     *
     * @param index     Position where to insert the listener.
     * @param listener  The new listener.
     *
     * @return <code>true</code> if the listener has been added,
     *         <code>false</code> otherwise (e.g. if already listening).
     */
    public final boolean addListener(final int index, final ValidatorListener listener){
        // Forbid NULL listeners:
        if (listener == null)
            return false;

        // Do nothing more if already registered:
        else if (listeners.contains(listener))
            return true;

        // Otherwise, use the standard ArrayList `add(int, Object)` function:
        try {
            listeners.add(index, listener);
            return true;
        }

        // ...but in case of error, log a WARNING and return `false`:
        catch(Exception ex){
            LOGGER.log(Level.WARNING, "Impossible to add the given listener! Cause: "+ex.getMessage(), ex);
            return false;
        }
    }

    /**
     * Remove the given listener from the list of objects listening to this
     * validator.
     *
     * <p><i><b>Note:</b>
     *  If the listener can not be removed, this function returns
     *  <code>false</code> and logs the reason as a FINE message.
     * </i></p>
     *
     * @param listener  The listener to remove.
     *
     * @return  <code>true</code> if the listener has been removed,
     *          <code>false</code> otherwise (e.g. if already not listening).
     */
    public final boolean removeListener(final ValidatorListener listener){
        // Use the standard ArrayList `remove(Object)` function:
        try {
            if (listeners.remove(listener))
                return true;
            else {
                LOGGER.log(Level.FINE, "Impossible to remove the given listener! Cause: it does not listen to this validator.");
                return false;
            }
        }
        // ...but in case of error, log a FINE message and return `false`:
        catch(Exception ex){
            LOGGER.log(Level.FINE, "Impossible to remove the given listener! Cause: "+ex.getMessage(), ex);
            return false;
        }
    }

    /**
     * Remove the specified listener from the list of objects listening to this
     * validator.
     *
     * <p><i><b>Note:</b>
     *  If no listener matches the given index, NULL is returned and the reason
     *  is logged as a FINE message.
     * </i></p>
     *
     * @param index  Index of the listener to remove.
     *
     * @return  The removed listener,
     *          or NULL in case of error.
     */
    public final ValidatorListener removeListener(final int index){
        try {
            return listeners.remove(index);
        }
        // ...but in case of error, log a WARNING and return NULL:
        catch(Exception ex){
            LOGGER.log(Level.FINE, "Impossible to remove the listener at the index "+index+"! Cause: "+ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * Get an iterator over the complete list of objects listening to this
     * validator.
     *
     * @return  An iterator over all listeners.
     */
    public final Iterator<ValidatorListener> getListeners(){
        return listeners.iterator();
    }

    /* *************************************************************************
     * PARSERS MANAGEMENT
     */

    /**
     * Get a parser for the given version of ADQL.
     *
     * <p>
     *     If no parser exists for the given ADQL version, one will be created
     *     and returned. Then, it will be reused whenever a parser for the same
     *     ADQL version is asked.
     * </p>
     *
     * <p>
     *     The returned parser always accepts all coordinate systems.
     * </p>
     *
     * @param version   The target ADQL version.
     *                  NULL is equivalent to {@link ValidationSetParser#DEFAULT_ADQL_VERSION}.
     *
     * @return  The corresponding ADQL parser.
     */
    protected ADQLParser getParser(ADQLVersion version) {
        // Set a default version if none is provided:
        if (version == null)
            version = ValidationSetParser.DEFAULT_ADQL_VERSION;

        // Try to get the parser, if already created:
        ADQLParser parser = parsers.get(version);

        // If not existing, create it:
        if (parser == null) {
            parsers.put(version, (parser = new ADQLParser(version)));
            // Allow any coordinate system from ADQL-2.1 only:
            /*if (version != ADQLVersion.V2_0){*/
                try {
                    parser.setAllowedCoordSys(null);
                } catch (ParseException e) {
                    LOGGER.log(Level.WARNING, "Impossible to remove the restriction on the coordinate system argument!", e);
                }
            /*}*/
        }

        // Return the found/created parser:
        return parser;
    }

    /* *************************************************************************
     * XML VALIDATION
     */

    /**
     *
     * Check that the document provided by the given stream is a valid XML
     * document, as expected by this {@link ADQLValidator}.
     *
     * @param stream    Stream toward the document to parse.
     *
     * @return boolean  <code>true</code> if valid XML document,
     *                  <code>false</code> otherwise (the error has already been
     *                  reported in registered listeners).
     *
     * @see XMLValidationSetParser#checkXML(InputStream)
     */
    public boolean checkXML(final InputStream stream) {
        try {
            (new XMLValidationSetParser()).checkXML(stream);
            return true;
        }catch(cds.adql.validation.parser.ParseException pe){
            publishError(new ValidationException("Incorrect XML syntax! Cause: "+pe.getMessage(), pe));
            publishEndValidation(new ValidationSet());
            return false;
        }
    }

    /**
     * Validate a valid XML document representing a complete validation set.
     *
     * <p>
     *     The parsed validation set will be validated thanks to
     *     {@link #validate(ValidationSet, String)}.
     * </p>
     *
     * <p>
     *     Validation status, progress, success and failure are all reported to
     *     all registered listeners.
     * </p>
     *
     * @param stream    Stream toward the XML validation set.
     * @param source    Human information about where the document comes from
     *                  (example: <code>File /foo/bar.xml</code>). It is purely
     *                  informal. It aims to improve the documentation of the
     *                  validation process.
     *
     * @return  <code>true</code> if all tests passed inside the validation set,
     *          <code>false</code> in case of error or if at least one test
     *          failed.
     *
     * @see XMLValidationSetParser#parse(InputStream)
     * @see #validate(ValidationSet, String)
     */
    public boolean validateXML(final InputStream stream, final String source){
        // Parse the file:
        ValidationSet tests;
        try{
            // Parse the XML document and transform it into a tests set:
            tests = (new XMLValidationSetParser()).parse(stream);
            // Validate the tests set:
            if (tests != null)
                return validate(tests, source);
            // Or report an error:
            else {
                publishError(new ValidationException("No validation set provided!"));
                return false;
            }
        }
        catch (cds.adql.validation.parser.ParseException e) {
            publishError(new ValidationException("XML document parsing failed! Cause: "+e.getMessage(), e));
            return false;
        }
    }

    /* *************************************************************************
     * GENERIC VALIDATION
     */

    /**
     * Validate all queries of the given validation set.
     *
     * <p>
     *     This function does NOT stop the validation at the first failed query.
     *     All queries are tested so that the final report can be as complete as
     *     possible.
     * </p>
     *
     * @param set       Set of queries to validate.
     * @param source    Human information about where the document comes from
     *                  (example: <code>File /foo/bar.xml</code>). It is purely
     *                  informal. It aims to improve the documentation of the
     *                  validation process.
     *
     * @return  <code>true</code> if all queries of the validation set passed,
     *          <code>false</code> in case of error, NULL or if one query failed.
     */
    public boolean validate(final ValidationSet set, final String source){
        // Nothing to validate if NULL:
        if (set == null)
            return false;

        // Publish the start of the Validation session:
        publishStartValidation(set, source);

        // Validate all queries:
        boolean allValid = true;
        for(ValidationQuery query : set.queries) {
            /* always try to validate all queries even if allValid is false ;
             * that way, all errors of all queries are reported. */
            allValid = validate(query, set.functions) && allValid;
        }

        // Publish the end of the Validation session:
        publishEndValidation(set);

        return allValid;
    }

    /**
     * Validate a single ADQL query.
     *
     * <p><i><b>Note:</b>
     *   NULL or an empty query string will make this function immediately
     *   return <code>false</code>.
     * </i></p>
     *
     * @param query     The query to validate.
     *
     * @return  <code>true</code> if this query passed the validation test,
     *          <code>false</code> otherwise.
     */
    public boolean validate(final ValidationQuery query){
        return validate(query, Collections.EMPTY_SET);
    }

    /**
     * Validate a single ADQL query.
     *
     * <p><i><b>Note:</b>
     *   NULL or an empty query string will make this function immediately
     *   return <code>false</code>.
     * </i></p>
     *
     * @param query     The query to validate.
     * @param functions Global UDFs to support while validating the query.
     *
     * @return  <code>true</code> if this query passed the validation test,
     *          <code>false</code> otherwise.
     */
    public boolean validate(final ValidationQuery query, final Set<UDF> functions){
        // Nothing to validate if NULL:
        if (query == null
            || query.query == null
            || query.query.trim().isEmpty())
        {
            return false;
        }

        boolean valid;
        String err = null;

        // Get the appropriate ADQL parser:
        final ADQLParser parser = getParser(query.adqlVersion);

        // Declare all UDFs to support:
        for(UDF u : functions)
            parser.getSupportedFeatures().support(u.getFeature());
        for(UDF u : query.functions)
            parser.getSupportedFeatures().support(u.getFeature());

        // Publish start of validation:
        publishStartValidation(query);

        // Parse the ADQL query:
        try {
            parser.parseQuery(query.query);
            valid = true;
        }catch(ParseException pe){
            valid = false;
            err = pe.getMessage();
        }

        // Report the validation result:
        final boolean success = (valid == query.isValid);
        publishTestResult(query, success, err);
        return success;
    }

    protected void publishStartValidation(final ValidationSet set,
                                          final String source)
    {
        for(ValidatorListener listener : listeners){
            listener.start(set, source);
        }
    }

    protected void publishEndValidation(final ValidationSet set){
        for(ValidatorListener listener : listeners){
            listener.end(set);
        }
    }

    protected void publishStartValidation(final ValidationQuery query){
        for(ValidatorListener listener : listeners){
            listener.validating(query);
        }
    }

    protected void publishError(final ValidationException error){
        for(ValidatorListener listener : listeners){
            listener.error(error);
        }
    }

    protected void publishTestResult(final ValidationQuery query,
                                     final boolean success,
                                     final String errorMessage)
    {
        for(ValidatorListener listener : listeners){
            if (success)
                listener.pass(query);
            else
                listener.fail(query, errorMessage);
        }
    }

}
