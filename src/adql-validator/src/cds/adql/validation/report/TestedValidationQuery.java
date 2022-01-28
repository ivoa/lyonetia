package cds.adql.validation.report;

import cds.adql.validation.query.ValidationQuery;

public class TestedValidationQuery {

    public final long index;

    /** The tested query. */
    public final ValidationQuery query;

    /** Indicate whether this query has been tested. */
    public boolean tested = false;

    /** Indicate whether the test passed (<code>true</code>) or failed
     * (<code>false</code>). */
    public boolean passed = true;

    /** Test duration (in ms).
     * <p><i><code>-1</code> (or any negative value) if not set.</i></p> */
    public long duration = -1;

    /** Message describing the test failure. */
    public String error = null;

    public TestedValidationQuery(final long index, final ValidationQuery query) {
        this.index = index;
        this.query = query;
    }

}
