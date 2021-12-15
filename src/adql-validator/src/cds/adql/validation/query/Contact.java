package cds.adql.validation.query;

import java.net.URL;

/**
 * Identity of the person to contact about the content of a validation tests
 * set.
 *
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 1.0 (09/2021)
 */
public class Contact {

    public String name = null;

    public URL url = null;

    @Override
    public String toString() {
        return "Contact{name='" + name + '\'' + ", url=" + url + '}';
    }
}
