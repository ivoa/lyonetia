package cds.adql.validation.report;

/**
 * Object collecting and providing statistics about an ADQL validation session.
 *
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 1.0 (09/2021)
 *
 * @see StatCollector
 */
public interface ValidationStatistics {

    /**
     * Get the total number of all validation queries.
     *
     * @return  Total number of tests.
     */
    long getCntTests();

    /**
     * Get the number of successful tests among {@link #getCntTests()}.
     *
     * <p>
     *     This function may just return the difference between
     *     {@link #getCntTests()} and {@link #getCntFail()}.
     * </p>
     *
     * @return  Number of successful tests.
     */
    long getCntPass();

    /**
     * Compute the number of failed tests among {@link #getCntTests()}.
     *
     * <p>
     *     This function may just return the difference between
     *     {@link #getCntTests()} and {@link #getCntPass()}.
     * </p>
     *
     * @return  Number of failed tests.
     */
    long getCntFail();

    /**
     * Get the accumulated duration of all tests.
     *
     * <p>To get the time in milli-seconds (or any other time unit):</p>
     * <pre>TimeUnit.NANOSECONDS.toMillis(collector.getTotalDuration())</pre>
     *
     * @return  Total duration (in nanoseconds) of the validation set.
     */
    long getTotalDuration();

    /**
     * Compute the average duration of a validation test.
     *
     * <p>
     *     This function may just return {@link #getTotalDuration()} divided by
     *     {@link #getCntTests()}.
     * </p>
     *
     * @return  Average duration (in nanoseconds) of a single validation test.
     */
    long getAvgDuration();

    /**
     * Get the duration of the last validation test.
     *
     * <p>To get the time in milli-seconds (or any other time unit):</p>
     * <pre>TimeUnit.NANOSECONDS.toMillis(collector.getLastTestDuration())</pre>
     *
     * @return  Last test's duration (in nano-seconds).
     */
    long getLastTestDuration();
}
