package cds.adql.validation.report;

import cds.adql.validation.ValidationException;
import cds.adql.validation.query.Contact;
import cds.adql.validation.query.Publisher;
import cds.adql.validation.query.ValidationQuery;
import cds.adql.validation.query.ValidationSet;

import java.io.PrintStream;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoField;
import java.util.*;

/**
 * Print the output of an {@link cds.adql.validation.ADQLValidator} into a
 * Markdown format.
 *
 * <p>
 *     This {@link ValidatorListener} accepts a {@link StatCollector} in
 *     argument of its constructor. This is useful to collect statistics in the
 *     same time and then to print them.
 * </p>
 *
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 1.0 (12/2021)
 */
public class MarkdownReport implements ValidatorListener {

    /** Date format for the test's execution date. */
    protected final static DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime( FormatStyle.MEDIUM )
                                                                          .withResolverFields(ChronoField.SECOND_OF_MINUTE)
                                                                          .withLocale( Locale.US )
                                                                          .withZone( ZoneId.of("UTC") );

    /** Standard output stream. */
    protected final PrintStream out;

    /** Error output stream. */
    protected final PrintStream err;

    /** Indicate whether all tests or only failed ones should be reported.
     * By default, <code>false</code>. */
    protected boolean showOnlyFailures = false;

    /** List of all tested queries. */
    protected List<TestedValidationQuery> testedQueries = new ArrayList<>();

    /** List of all validation exception/errors that may have unexpectedly
     * happened. */
    protected List<ValidationException> errors = new ArrayList<>();

    /** Formatted date of the time when the validation started to be validated.
     * Reset by {@link #start(ValidationSet, String)}. */
    protected String startDate = null;

    /** Ordered index of the test in the entire validation set. */
    protected long indexTest = 0;

    /** Number of available tests in the in-progress validation set. */
    protected long nbAvailableTests = 0;

    /** Number of successful tests. */
    protected long nbPassed = 0;

    /** Output just collecting statistics about a validation session. */
    protected ValidationStatistics validationStats = null;

    /**
     * Create a {@link MarkdownReport} with <code>System.out</code> for
     * normal messages and <code>System.err</code> for errors.
     */
    public MarkdownReport() {
        this(System.out, System.err);
    }

    /**
     * Create a {@link MarkdownReport} with the given stream for
     * normal messages and <code>System.err</code> for errors.
     *
     * @param output    Stream for normal messages.
     */
    public MarkdownReport(final PrintStream output) {
        this(output, System.err);
    }

    /**
     * Create a {@link MarkdownReport} with the given streams for the
     * normal messages and errors.
     *
     * @param output        Stream for normal messages.
     *                      <i>If NULL, <code>System.out</code>.</i>
     * @param error         Stream for error messages.
     *                      <i>If NULL, <code>System.err</code>.</i>
     */
    public MarkdownReport(final PrintStream output, final PrintStream error) {
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
        startDate = null;
        testedQueries.clear();
        errors.clear();
    }

    @Override
    public void start(final ValidationSet set, final String source) {
        // Nothing to do if no validation set:
        if (set == null)
            return;

        // Display a description of the validation tests set:
        out.println("# ADQL Validation report");
        out.println();
        out.println();
        out.println("## About the validation set");
        out.println();
        out.println("| _Key_               | _Value_                                                      |");
        out.println("| ------------------- | ------------------------------------------------------------ |");
        out.println("| **Origin**          | " + ((source != null && source.trim().length() > 0) ? source : "-") + " |");
        out.println("| **Publisher**       | " + formatPublisher(set.publisher) + " |");
        out.println("| **Contact**         | " + formatContact(set.contact) + " |");
        out.println("| **Description**     | " + (set.description != null ? set.description : "-") + " |");
        out.println("| **Available tests** | " + set.queries.size() + " |");

        // Remember the max. number of tests to run:
        nbAvailableTests = set.queries.size();

        // Set the start date:
        startDate = getCurrentUTCTime();
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

        if (!showOnlyFailures) {
            // Create the query result:
            final TestedValidationQuery testedQuery = new TestedValidationQuery(indexTest, query);
            testedQuery.tested = true;
            testedQuery.passed = true;
            if (validationStats != null)
                testedQuery.duration = validationStats.getLastTestDuration();

            // Keep it:
            testedQueries.add(testedQuery);
        }
    }

    @Override
    public void fail(final ValidationQuery query, final String errorMessage) {
        // Nothing to do if no query:
        if (query == null)
            return;

        // Create the query result:
        final TestedValidationQuery testedQuery = new TestedValidationQuery(indexTest, query);
        testedQuery.tested = true;
        testedQuery.passed = false;
        if (validationStats != null)
            testedQuery.duration = validationStats.getLastTestDuration();

        // Print the cause of failure:
        if (errorMessage != null && errorMessage.trim().length() > 0)
            testedQuery.error = "Parsing error: "+errorMessage;
        else
            testedQuery.error = "Unexpected test result.";

        // Keep it:
        testedQueries.add(testedQuery);
    }

    @Override
    public void end(final ValidationSet set) {
        // Nothing to do, if no validation set:
        if (set == null)
            return;

        // Write the summary:
        out.println();
        out.println();
        out.println("## Summary");
        out.println();
        out.println("| GLOBAL RESULT             | " + (nbPassed == nbAvailableTests ? "OK" : (nbAvailableTests-nbPassed)+" FAILED") + " |");
        out.println("| ------------------------- | ------------------------------------------------------------ |");
        out.println("| **Successful tests**      | "  + nbPassed + " (" + (nbAvailableTests == 0 ? 100 : (int)(100.0 * nbPassed / nbAvailableTests)) + "%) |");
        out.println("| **Total duration**        | " + (validationStats != null ? validationStats.getTotalDuration() / ((float) 1e9) + "s" : "-") + " |");
        out.println("| **Average test duration** | " + (validationStats != null ? validationStats.getAvgDuration() / ((float) 1e6) + "ms" : "-" ) + " |");
        out.println("| **Execution date (UTC)**  | " + getCurrentUTCTime() + " |");

        // Write the validation tests listing:
        if (!testedQueries.isEmpty()) {
            out.println();
            out.println();
            out.println("## Validation tests");
            out.println();
            out.println("### Listing");
            out.println();
            out.println("|    #    |                    UUID                    |    Status    |");
            out.println("| :-----: | :----------------------------------------: | :----------: |");
            testedQueries.sort((t1, t2) -> {
                if (t1.passed == t2.passed)
                    return Long.compare(t1.index, t2.index);
                else if (t1.passed)
                    return 1;
                else
                    return -1;
            });
            for (TestedValidationQuery q : testedQueries)
                out.println("| [" + q.index + "](#" + q.index + ") | [" + q.query.id + "](#" + q.index + ") | [" + (q.passed ? "OK" : "FAILED") + "](#" + q.index + ") |");

            // Write the validation tests details:
            testedQueries.sort(Comparator.comparingLong(t -> t.index));
            for (TestedValidationQuery q : testedQueries) {
                out.println();
                out.println();
                out.println();
                out.println("### " + q.index);
                out.println();
                out.println("* **UUID:** `" + q.query.id + "`");
                out.println();
                out.println("* **Description:**");
                if (q.query.description != null) {
                    out.println();
                    out.println("  > " + q.query.description.replaceAll("\n", "\n  > "));
                }
                out.println();
                out.println("* **Query:**");
                if (q.query.query != null) {
                    out.println();
                    out.println("  ```sql");
                    out.println("  " + q.query.query.replaceAll("\n", "\n  "));
                    out.println("  ```");
                }
                out.println();
                out.println("* **ADQL version:** " + q.query.adqlVersion);
                out.println();
                out.println("* **Expected:** " + (q.query.isValid ? "success" : "failure"));
                out.println();
                out.println("* **Test:**");
                out.println();
                out.println("  * **Duration:** " + (q.duration >= 0 ? (q.duration / 1e6) + "ms" : "-"));
                out.println();
                out.println("  * **Status:** " + (q.passed ? "OK" : "FAILED"));
                if (!q.passed && q.error != null) {
                    out.println();
                    out.println("  * **Error:**");
                    out.println();
                    out.println("    > " + q.error.replaceAll("\n", "\n    > "));
                }
            }
        }

        // End with errors, if any:
        if (!errors.isEmpty()){
            out.println();
            out.println();
            out.println();
            out.println("## General errors");
            int i=0;
            for(ValidationException err : errors){
                out.println();
                out.print("* Error #" + (++i) + ":");
                out.println();
                final String errMsg = err.getMessage();
                if (errMsg == null || errMsg.trim().length() == 0)
                    out.println("  > <"+err.getClass().getName() + ">");
                else
                    out.println("  > " + err.getMessage());
            }

        }
    }

    @Override
    public void error(final ValidationException error) {
        if (error != null)
            errors.add(error);
    }

    /**
     * Give the serialized form of the current UTC time.
     *
     * @return The serialized UTC time.
     */
    protected static String getCurrentUTCTime() {
        return formatter.format( Instant.now() );
    }

    /**
     * Format a publisher into a Markdown format.
     *
     * @param publisher The publisher to format.
     *
     * @return  The formatted publisher,
     *          <code>-</code> if NULL.
     *
     * @see #formatContact(String, URL)
     */
    protected final String formatPublisher(final Publisher publisher){
        return (publisher == null) ? formatContact(null, null) : formatContact(publisher.name, publisher.url);
    }

    /**
     * Format a contact into a Markdown format.
     *
     * @param contact The contact to format.
     *
     * @return  The formatted contact,
     *          <code>-</code> if NULL.
     *
     * @see #formatContact(String, URL)
     */
    protected final String formatContact(final Contact contact) {
        return (contact == null) ? formatContact(null, null) : formatContact(contact.name, contact.url);
    }

    /**
     * Format a contact name and URL into a Markdown format.
     *
     * <p>
     *     If no name and no URL is provided, just the string <code>-</code> is
     *     returned.
     * </p>
     *
     * <p>
     *     If a URL is provided but no name, only the URL is displayed.
     *     Examples: <code>&lt;http://contact.url.com&gt;</code>, or
     *     <code>[me@ivoa.net](mailto:me@ivoa.net)</code> if a MailTo URL.
     * </p>
     *
     * <p>
     *     If a name and a URL are both provided, the name linked toward the URL
     *     is displayed.
     *     Examples: <code>[My Name](http://contact.url.com)</code>,
     *     <code>[My Name](mailto:contact@email.com)</code>
     * </p>
     *
     * @param name Contact name.
     * @param url Contact's URL.
     *
     * @return  The formatted contact,
     *          <code>-</code> if NULL.
     */
    protected String formatContact(final String name, final URL url){
        // Normalize the contact name:
        final String contactName = (name == null || name.trim().length() == 0 ? "" : name.trim());

        // No contact or no name + no URL => nothing to show ("-"):
        if (contactName.isEmpty() && url == null){
            return "-";
        }
        // Otherwise, show the name and/or the URL:
        else {
            // No URL => only the name:
            if (url == null)
                return contactName;
            // No name => only the URL
            else if (contactName.isEmpty()) {
                // ...if mailto => display and link the email address:
                if ("mailto".equalsIgnoreCase(url.getProtocol()))
                    return "[" + url.getPath() + "](" + url + ")";
                // ...if normal URL => display and link the URL:
                else
                    return "<" + url + ">";
            }
            // Both name and URL => display and link the name:
            else
                return "[" + contactName + "](" + url + ")";
        }
    }
}
