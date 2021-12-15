package cds.adql.validation.query;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Representation of a set of Validation Tests.
 *
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 1.0 (09/2021)
 */
public class ValidationSet {

    /** Person/Entity to contact in case of question/comment about this set. */
    public Contact contact = null;

    /** Person/Entity who published this set. */
    public Publisher publisher = null;

    /** Human description of this entire set.*/
    public String description = null;

    /** Set of all Validation Tests included inside this set.
     * <p>
     *     It contains only unique items: no two {@link ValidationQuery}
     *     instances have the same UUID.
     * </p> */
    public final Set<ValidationQuery> queries = new LinkedHashSet<>(30);

}
