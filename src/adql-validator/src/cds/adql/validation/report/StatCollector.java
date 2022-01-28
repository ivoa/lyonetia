package cds.adql.validation.report;

import cds.adql.validation.ValidationException;
import cds.adql.validation.query.ValidationQuery;
import cds.adql.validation.query.ValidationSet;

/**
 * Collect simple statistics about the validation of a set of queries:
 *
 * <ul>
 *     <li>total number of validation tests,</li>
 *     <li>number of successful validation tests,</li>
 *     <li>accumulated duration of all tests.</li>
 * </ul>
 *
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 1.0 (09/2021)
 */
public class StatCollector implements ValidatorListener, ValidationStatistics {

    /** Number of overall validation tests.
     * <p><i>Reset to 0 by {@link #clear()}.</i></p> */
    protected int cntTests = 0;

    /** Number of successful tests.
     * <p><i>Reset to 0 by {@link #clear()}.</i></p> */
    protected int cntPass = 0;

    /** Accumulated duration (in nanoseconds) of all tests.
     * <p><i>Reset to 0 by {@link #clear()}.</i></p> */
    protected long totalDuration = 0;

    /** Duration (in nanoseconds) of the last validation test.
     * <p><i>Reset to 0 by {@link #clear()}.</i></p> */
    protected long lastTestDuration = 0;

    /** Time (in nanoseconds) when the last validation test started.
     * <p><i>
     *     Reset to 0 by {@link #clear()} and {@link #appendTestDuration()}.
     * </i></p> */
    protected long startTest = 0;

    @Override
    public boolean isShowOnlyFailures() {
        // Everything is always counted.
        return false;
    }

    @Override
    public void setShowOnlyFailures(boolean showOnlyFailures) {
        // Everything is always counted. So, no filter should be set.
    }

    @Override
    public void setValidationStats(ValidationStatistics validationStats) {
        // Not interesting to listen another statistics collector.
    }

    /**
     * Get the total number of all validation queries.
     *
     * <p><i>Reset to 0 by {@link #clear()}.</i></p>
     *
     * @return  Total number of tests.
     */
    public final long getCntTests() {
        return cntTests;
    }

    /**
     * Get the number of successful tests among {@link #getCntTests()}.
     *
     * <p><i>Reset to 0 by {@link #clear()}.</i></p>
     *
     * @return  Number of successful tests.
     */
    public final long getCntPass() {
        return cntPass;
    }

    /**
     * Compute the number of failed tests among {@link #getCntTests()}.
     *
     * <p>
     *     This function just returns the difference between
     *     {@link #getCntTests()} and {@link #getCntPass()}.
     * </p>
     *
     * @return  Number of failed tests.
     */
    public final long getCntFail() {
        return cntTests - cntPass;
    }

    /**
     * Get the accumulated duration of all tests.
     *
     * <p><i>Reset to 0 by {@link #clear()}.</i></p>
     *
     * <p>To get the time in milli-seconds (or any other time unit):</p>
     * <pre>TimeUnit.NANOSECONDS.toMillis(collector.getTotalDuration())</pre>
     *
     * @return  Total duration (in nanoseconds) of the validation set.
     */
    public final long getTotalDuration(){
        return totalDuration;
    }

    /**
     * Compute the average duration of a validation test.
     *
     * <p>
     *     This function just returns {@link #getTotalDuration()} divided by
     *     {@link #getCntTests()}.
     * </p>
     *
     * @return  Average duration (in nanoseconds) of a single validation test.
     */
    public final long getAvgDuration(){
        return (cntTests > 0 ? (totalDuration / cntTests) : 0);
    }

    /**
     * Get the duration of the last validation test.
     *
     * <p><i>Reset to 0 by {@link #clear()}.</i></p>
     *
     * <p>To get the time in milli-seconds (or any other time unit):</p>
     * <pre>TimeUnit.NANOSECONDS.toMillis(collector.getLastTestDuration())</pre>
     *
     * @return  Last test's duration (in nano-seconds).
     */
    public final long getLastTestDuration(){
        return lastTestDuration;
    }

    /**
     * Compute the duration of the last validation test and append it to
     * {@link #getTotalDuration()}.
     *
     * <p>
     *     This function resets {@link #startTest} to 0.
     * </p>
     */
    protected void appendTestDuration(){
        if (startTest > 0) {
            lastTestDuration = System.nanoTime() - startTest;
            totalDuration += lastTestDuration;
            startTest = 0;
        }
    }

    @Override
    public void clear() {
        cntTests = 0;
        cntPass = 0;
        totalDuration = 0;
        lastTestDuration = 0;
    }

    @Override
    public void start(ValidationSet set, String source) {}

    @Override
    public void validating(ValidationQuery query) {
        cntTests++;
        startTest = System.nanoTime();
    }

    @Override
    public void pass(ValidationQuery query) {
        if (startTest > 0) {
            cntPass++;
            appendTestDuration();
        }
    }

    @Override
    public void fail(ValidationQuery query, String errorMessage) {
        if (startTest > 0) {
            appendTestDuration();
        }
    }

    @Override
    public void end(ValidationSet set) {}

    @Override
    public void error(ValidationException error) {}

}
