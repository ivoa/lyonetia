package cds.adql.validation.query;

import cds.adql.validation.parser.ValidationSetParser;

import java.util.Objects;
import java.util.UUID;

import static adql.parser.ADQLParser.ADQLVersion;

/**
 * Representation of a single Validation Test corresponding to an ADQL query to
 * parse and check.
 *
 * <p><b>IMPORTANT:</b>
 *     A {@link ValidationQuery} MUST have a unique UUID. If none is provided at
 *     initialization, one will be automatically created.
 * </p>
 *
 * <p>
 *     Though by default, there is no ADQL query set, one should immediately be
 *     created. Otherwise this Validation Test is not useful.
 * </p>
 *
 * <p><b>IMPORTANT:</b>
 *     Two {@link ValidationQuery} instances are considered as equals if they
 *     have exactly the same UUID. No other piece of information (e.g. the ADQL
 *     query) is checked.
 * </p>
 *
 * <i>
 * <p><b>Notes:</b></p>
 * <ul>
 *     <li>
 *        Do not forget to set appropriately the flag about the expected test
 *        result: see {@link #isValid}.
 *     </li>
 *     <li>
 *        The highest possible target ADQL version should also be set if not
 *        corresponding to the latest in date: see {@link #adqlVersion} (by
 *        default: {@link ValidationSetParser#DEFAULT_ADQL_VERSION DEFAULT_ADQL_VERSION}.
 *     </li>
 * </ul>
 * </i>
 *
 *
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 1.0 (09/2021)
 */
public class ValidationQuery {

    /** Unique ID of this test in an entire validation tests set. */
    public final UUID id;

    /** Human description of this test. */
    public String description = null;

    /** The ADQL query to test. */
    public String query = null;

    /** Indicate whether the query is expected to be valid or not. */
    public boolean isValid = false;

    /** Query's ADQL version target. */
    public ADQLVersion adqlVersion = ValidationSetParser.DEFAULT_ADQL_VERSION;

    /**
     * Create a {@link ValidationQuery} with a generated UUID.
     */
    public ValidationQuery(){
        this((UUID)null);
    }

    /**
     * Create a {@link ValidationQuery} with the given UUID.
     *
     * @param id    UUID of this validation test.
     *              <i>If NULL, one will be automatically generated.</i>
     */
    public ValidationQuery(final UUID id){
        this.id = (id == null) ? UUID.randomUUID() : id;
    }

    /**
     * Create a {@link ValidationQuery} with the given UUID.
     *
     * @param id    UUID of this validation test.
     *              <i>If NULL, one will be automatically generated.</i>
     */
    public ValidationQuery(final String id){
        this.id = (id == null || id.trim().length() == 0) ? UUID.randomUUID() : UUID.fromString(id);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ValidationQuery testQuery = (ValidationQuery) o;
        return id.equals(testQuery.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
