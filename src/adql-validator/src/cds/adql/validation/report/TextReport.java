package cds.adql.validation.report;

import cds.adql.validation.ValidationException;
import cds.adql.validation.query.Contact;
import cds.adql.validation.query.Publisher;
import cds.adql.validation.query.ValidationQuery;
import cds.adql.validation.query.ValidationSet;

import java.io.PrintStream;

/**
 * Print the output of an {@link cds.adql.validation.ADQLValidator} into a text
 * format.
 *
 * <p>
 *     This {@link ValidatorListener} accepts a {@link StatCollector} in
 *     argument of its constructor. This is useful to collect statistics in the
 *     same time and then to print them.
 * </p>
 *
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 1.0 (10/2022)
 */
public class TextReport implements ValidatorListener {

    /** Standard output stream. */
    protected final PrintStream out;

    /** Error output stream. */
    protected final PrintStream err;

    /** Indicate whether all tests or only failed ones should be reported.
     * By default, <code>false</code>. */
    protected boolean showOnlyFailures = false;

    /** Ordered index of the test in the entire validation set. */
    protected long indexTest = 0;

    /** Number of successful tests. */
    protected long nbPassed = 0;

    /** Number of available tests in the in-progress validation set. */
    protected long nbAvailableTests = 0;

    /** Output just collecting statistics about a validation session. */
    protected ValidationStatistics validationStats = null;

    protected final static String SEPARATOR = "================================================================================";
    protected final static String LIGHT_SEP = "--------------------------------------------------------------------------------";

    /**
     * Create a {@link TextReport} with <code>System.out</code> for
     * normal messages and <code>System.err</code> for errors.
     */
    public TextReport() {
        this(System.out, System.err);
    }

    /**
     * Create a {@link TextReport} with the given stream for
     * normal messages and <code>System.err</code> for errors.
     *
     * @param output    Stream for normal messages.
     */
    public TextReport(final PrintStream output) {
        this(output, System.err);
    }

    /**
     * Create a {@link TextReport} with the given streams for the
     * normal messages and errors.
     *
     * @param output    Stream for normal messages.
     *                  <i>If NULL, <code>System.out</code>.</i>
     * @param error     Stream for error messages.
     *                  <i>If NULL, <code>System.err</code>.</i>
     */
    public TextReport(final PrintStream output, final PrintStream error) {
        if (output == null)
            out = System.out;
        else
            out = output;

        if (error == null)
            err = System.err;
        else
            err = error;
    }

    public final boolean isShowOnlyFailures() {
        return showOnlyFailures;
    }

    public final void setShowOnlyFailures(final boolean showOnlyFailures) {
        this.showOnlyFailures = showOnlyFailures;
    }

    public final void setValidationStats(final ValidationStatistics validationStats){
        this.validationStats = validationStats;
    }

    @Override
    public void clear() {
        indexTest = 0;
        nbAvailableTests = 0;
        nbPassed = 0;
    }

    @Override
    public void start(final ValidationSet set, final String source) {
        // Nothing to do if no validation set:
        if (set == null)
            return;

        // Display a description of the validation tests set:
        out.println("Starting validation...");
        out.println(SEPARATOR);
        out.println("Title          : "+formatText(set.title, "-"));
        out.println("Origin         : "+((source != null && source.trim().length() > 0) ? source : "-"));
        out.println("Publisher      : "+formatPublisher(set.publisher));
        out.println("Contact        : "+formatContact(set.contact));
        out.println("Description    : "+formatText(set.description, "-"));
        out.println("Available tests: "+set.queries.size());
        out.println(SEPARATOR);

        // Remember the max. number of tests to run:
        nbAvailableTests = set.queries.size();
    }

    @Override
    public void validating(final ValidationQuery query) {
        // Nothing to do if no query:
        if (query == null)
            return;

        // Here is a new test to run:
        indexTest++;
    }

    @Override
    public void pass(final ValidationQuery query) {
        // Nothing to do if no query:
        if (query == null)
            return;

        // Keep score:
        nbPassed++;

        if (!isShowOnlyFailures())
        {
            // Query test's preamble:
            out.println(indexTest+"/"+nbAvailableTests+" - "+query.id+" - SUCCESS");

            // Optional: validation duration:
            if (validationStats != null)
                out.println("Executed in "+ validationStats.getLastTestDuration()/((float)1e6) + "ms");

            // Print the test's description:
            out.println(LIGHT_SEP);
            out.println("Description: " + (query.description == null || query.description.trim().length() == 0 ? "-" : query.description));
            out.println("Query      : " + (query.query == null || query.query.trim().length() == 0 ? "" : query.query));
            out.println("Expected   : " + (query.isValid ? "success" : "failure"));

            out.println(SEPARATOR);
        }
    }

    @Override
    public void fail(final ValidationQuery query, final String errorMessage) {
        // Nothing to do if no query:
        if (query == null)
            return;

        // Query test's preamble:
        out.println(indexTest+"/"+nbAvailableTests+" - "+query.id+" - FAILED");

        // Print the cause of failure:
        if (errorMessage != null && errorMessage.trim().length() > 0)
            out.println("Due to: a parsing error: "+errorMessage);
        else
            out.println("Due to: an unexpected result");

        // Print execution duration, if any:
        if (validationStats != null)
            out.println("Executed in "+ validationStats.getLastTestDuration()/((float)1e6) + "ms");

        // Print the test's description:
        out.println(LIGHT_SEP);
        out.println("Description: "+(query.description == null || query.description.trim().length() == 0 ? "-" : query.description));
        out.println("Query      : "+(query.query == null || query.query.trim().length() == 0 ? "" : query.query));
        out.println("Expected   : "+(query.isValid ? "success" : "failure"));

        out.println(SEPARATOR);
    }

    @Override
    public void end(final ValidationSet set) {
        // Nothing to do, if no validation set:
        if (set == null)
            return;

        // Validation status:
        out.println("Validation completed!");

        // Display general stats, if any:
        out.println(LIGHT_SEP);
        out.println("Summary : "+nbPassed+"/"+nbAvailableTests+ " successful tests ("+(nbAvailableTests <= 0 ? 100 : (int)(100.0*nbPassed/nbAvailableTests))+"%)");
        if (validationStats != null)
            out.println("Duration: total=" + validationStats.getTotalDuration() / ((float) 1e9) + "s, average=" + validationStats.getAvgDuration() / ((float) 1e6)+"ms");

        out.println(SEPARATOR);
    }

    @Override
    public void error(final ValidationException error) {
        if (error != null){
            final String errMsg = error.getMessage();
            err.println("ERROR: "+(errMsg == null || errMsg.trim().length() == 0 ? "<" + error.getClass().getName() + ">" : error.getMessage()));
        }
    }

    /**
     * Format a possibly long piece of text.
     *
     * Multiple space characters (e.g. space, line return, tab) are replaced by
     * a single space.
     *
     * @param txt           The text to format.
     * @param defaultValue  Value returned if the given text is NULL or empty.
     *
     * @return  The formatted text.
     */
    public static String formatText(final String txt, final String defaultValue){
        if (txt == null || txt.trim().length() == 0)
            return defaultValue;
        else
            return txt.replaceAll(System.getProperty("line.separator"), " ").replaceAll("\t", " ").replaceAll(" +", " ");
    }

    /**
     * Format a publisher into a text format.
     *
     * @param publisher The publisher to format.
     *
     * @return  The formatted publisher,
     *          <code>-</code> if NULL.
     */
    protected String formatPublisher(final Publisher publisher){
        // No publisher or no name + no URL => nothing to show ("-"):
        if (publisher == null || ((publisher.name == null || publisher.name.trim().length() == 0) && publisher.url == null))
            return "-";
            // Otherwise, show the name and/or the URL:
        else
            return (publisher.name == null || publisher.name.trim().length() == 0 ? "" : publisher.name)
                    + (publisher.url == null ? "" : " (" + ("mailto".equalsIgnoreCase(publisher.url.getProtocol()) ? publisher.url.getPath() : publisher.url) + ")");
    }

    /**
     * Format a contact into a text format.
     *
     * @param contact The contact to format.
     *
     * @return  The formatted contact,
     *          <code>-</code> if NULL.
     */
    protected String formatContact(final Contact contact){
        // No contact or no name + no URL => nothing to show ("-"):
        if (contact == null)
            return "-";
            // Otherwise, show the name and/or the URL:
        else
            return (contact.name == null || contact.name.trim().length() == 0 ? "" : contact.name) + (contact.url == null ? "" : " (" + ("mailto".equalsIgnoreCase(contact.url.getProtocol()) ? contact.url.getPath() : contact.url) + ")");
    }
}
