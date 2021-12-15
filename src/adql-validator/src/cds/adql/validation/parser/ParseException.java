package cds.adql.validation.parser;

/**
 * Exception thrown while parsing the XML representation of a
 * {@link cds.adql.validation.query.ValidationSet}.
 *
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 1.0 (09/2021)
 * @see ValidationSetParser
 */
public class ParseException extends Exception {

    public ParseException() {}

    public ParseException(String s) {
        super(s);
    }

    public ParseException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public ParseException(Throwable throwable) {
        super(throwable);
    }

}
