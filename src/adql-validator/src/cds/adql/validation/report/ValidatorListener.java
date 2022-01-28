package cds.adql.validation.report;

import cds.adql.validation.ValidationException;
import cds.adql.validation.query.ValidationQuery;
import cds.adql.validation.query.ValidationSet;

/**
 * Interface of an object listening for the progress of ADQL validation tests.
 *
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 1.0 (09/2021)
 */
public interface ValidatorListener {

    /** Clear any data/resources associated with the last validation session. */
    void clear();

    /**
     * Event triggered when starting a new validation session.
     *
     * @param set       Set of validation queries.
     * @param source    Origin of the validation set.
     */
    void start(final ValidationSet set, final String source);

    /**
     * Event triggered just before starting the validation of a test query.
     *
     * @param query The validation test to be started.
     */
    void validating(final ValidationQuery query);

    /**
     * Event triggered when the last validation test previously reported by
     * {@link #validating(ValidationQuery)} successfully completed.
     *
     * @param query The successful validation test.
     */
    void pass(final ValidationQuery query);

    /**
     * Event triggered when the last validation test previously reported by
     * {@link #validating(ValidationQuery)} failed.
     *
     * @param query         The failed validation test.
     * @param errorMessage  Error reported by the parser.
     */
    void fail(final ValidationQuery query, final String errorMessage);

    /**
     * Event triggered at the end of a validation session.
     *
     * @param set   Set of validation queries.
     */
    void end(final ValidationSet set);

    /**
     * Event triggered when an error occurred.
     *
     * @param error The error to report.
     */
    void error(final ValidationException error);

    /**
     * Tell whether only failed tests will be reported.
     *
     * @return <code>true</code> if only failed tests are reported,
     *         <code>false</code> if all are reported.
     */
    boolean isShowOnlyFailures();

    /**
     * Set whether only failed tests should be reported.
     *
     * @param showOnlyFailures <code>true</code> to report only failed tests,
     *                         <code>false</code> to report all tests.
     */
    void setShowOnlyFailures(final boolean showOnlyFailures);

    /**
     * Associate the given {@link ValidationStatistics} with this
     * {@link ValidatorListener}.
     *
     * <p>
     *     When associated with a {@link ValidationStatistics}, this
     *     {@link ValidatorListener} will report statistics after each
     *     validation test and then globally at the end.
     * </p>
     *
     * <p><b>Important:</b>
     *  Validation events are not propagated down to the associated
     *  {@link ValidationStatistics} by this instance of
     *  {@link ValidatorListener}. The latter just uses and displays statistics
     *  provided by the given {@link ValidationStatistics} object.
     * </p>
     *
     * @param validationStats Statistics about a validation process.
     *                        <i>If NULL, no statistics will be reported.</i>
     */
    void setValidationStats(final ValidationStatistics validationStats);

}
