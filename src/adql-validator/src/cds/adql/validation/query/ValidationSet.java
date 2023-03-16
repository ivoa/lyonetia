package cds.adql.validation.query;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Representation of a set of Validation Tests.
 *
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 1.0 (01/2023)
 */
public class ValidationSet {

    /** Person/Entity to contact in case of question/comment about this set. */
    public Contact contact = null;

    /** Person/Entity who published this set. */
    public Publisher publisher = null;

    /** Title of this set. It is mainly used in the validation report to nicely
     * identify each validation set. */
    public String title = null;

    /** Human description of this entire set.*/
    public String description = null;

    /** Definitions of all allowed User Defined Functions. */
    public Set<UDF> functions = new LinkedHashSet<>(3);

    /** Set of all Validation Tests included inside this set.
     * <p>
     *     It contains only unique items: no two {@link ValidationQuery}
     *     instances have the same UUID.
     * </p> */
    public final Set<ValidationQuery> queries = new LinkedHashSet<>(30);

}
