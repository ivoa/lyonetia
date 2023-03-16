package cds.adql.validation.parser.xml;

import cds.adql.validation.parser.ParseException;
import cds.adql.validation.parser.ValidationSetParser;
import cds.adql.validation.query.*;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Stack;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Parser for sets of ADQL validation queries serialized in XML.
 *
 * <p>
 *     The XML document to parse must follow the XSD file
 *     {@value #XSD_FILE}. The structure is checked against this XSD
 *     document only by the function {@link #checkXML(InputStream)}. The API
 *     function {@link #parse(InputStream)} does not use the XSD file, but
 *     expects the latter to be respected.
 * </p>
 *
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 1.0 (01/2023)
 */
public class XMLValidationSetParser implements ValidationSetParser {

    protected static final Logger LOGGER = Logger.getLogger(XMLValidationSetParser.class.getName());

    /** Name of the XSD file defining the XML structure for a validation set.
     * This file is searched in the class-path. */
    protected static final String XSD_FILE = "queries.xsd";

    @Override
    public ValidationSet parse(final InputStream stream) throws ParseException, NullPointerException {
        if (stream == null)
            throw new NullPointerException("Missing input validation set!");

        // Ensure it may be an XML document:
        checkFormat(stream);

        try {
            // Create the XML parser:
            final SAXParserFactory factory = SAXParserFactory.newInstance();
            final SAXParser saxParser = factory.newSAXParser();

            // Create the handler receiving parsing events:
            final QueriesHandler querieshandler = new QueriesHandler();

            // Parse the document:
            saxParser.parse(stream, querieshandler);

            // Return the parsed validation set:
            return querieshandler.queries;
        }
        catch (Exception e) {
            throw new ParseException(e);
        }
    }

    /**
     * Check that the given document is an XML document which matches the
     * expected XSD schema ({@link #XSD_FILE}).
     *
     * <p><i><b>Note:</b>
     *  At the end of this function, the given input stream is reset to its
     *  initial position.
     * </i></p>
     *
     * <p><i><b>IMPORTANT:</b>
     *  This function does nothing if the given input stream can not be reset
     *  ({@link InputStream#markSupported()}.
     * </i></p>
     *
     * @param stream    Stream to test.
     *
     * @throws ParseException   In case of error when accessing the stream.
     */
    public void checkXML(InputStream stream) throws ParseException {
        if (stream == null)
            throw new NullPointerException("Missing input validation set!");

        // Ensure it may be an XML document:
        checkFormat(stream);

        // Ensure the XML document follows the expected schema:
        validateXMLSchema(stream);
    }

    /**
     * Try to check whether the input is really an XML document.
     *
     * <p>
     *     This function only checks the beginning of the given stream. If
     *     starting with an opening XML tag, then it is considered as an XML
     *     document.
     * </p>
     *
     * <p><b>Warning:</b>
     *     This function has no effect if the given stream does not support
     *     byte marking (cf {@link InputStream#markSupported()}. Indeed, this
     *     feature is used to check the first bytes of the stream. After the
     *     check, the stream is reset to its original position in order to be
     *     properly parsed by an XML parser.
     * </p>
     *
     * <p><i><b>Note:</b>
     *  The idea of this test is to return a meaningful error message, otherwise
     *  a SAXException with the following mysterious message is thrown:
     *  <code>Content is not allowed in prolog.</code>.
     * </i></p>
     *
     * @param stream    Stream to test.
     *
     * @throws ParseException   If the given stream does not contain an XML
     *                          document.
     */
    protected void checkFormat(final InputStream stream) throws ParseException {
        if (stream != null && stream.markSupported()){
            try {
                byte[] bufHead = new byte[128];
                final int nbRead = stream.read(bufHead);
                final String head = (nbRead > 0 ? new String(bufHead, 0, nbRead) : "");
                final Pattern patternXMLTag = Pattern.compile("^<[^>]+>");
                if (nbRead < 3 || !patternXMLTag.matcher(head).find()){
                    throw new ParseException("Unsupported ADQL validation set format! Expected: XML document.");
                }
            }
            catch(IOException ioe) {
                // Silent error!
            }
            finally{
                try {
                    stream.reset();
                }catch(IOException ioe){
                    // Silent error!
                }
            }
        }
    }

    /**
     * Test whether the given stream follows the expected XSD schema.
     *
     * @param stream The stream to test.
     *
     * @throws ParseException   If the given stream does not match the expected
     *                          XSD schema.
     */
    protected void validateXMLSchema(final InputStream stream) throws ParseException {
        try {
            final SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            final Schema schema = factory.newSchema(new StreamSource(Thread.currentThread().getContextClassLoader().getResourceAsStream(XSD_FILE)));
            final Validator validator = schema.newValidator();
            validator.validate(new StreamSource(stream));
            LOGGER.fine("VALID XML DOCUMENT");
        }
        catch (IOException | SAXException ioe) {
            throw new ParseException(ioe);
        }
    }

    /**
     * XML parser's handler.
     *
     * @author Gr&eacute;gory Mantelet (CDS)
     * @version 1.0 (10/2022)
     */
    protected static class QueriesHandler extends DefaultHandler {

        /** Enumeration of all possible parsing Contexts. */
        private enum Context {
            QUERIES,
            QUERIES_TITLE,
            QUERIES_DESCRIPTION,
            QUERIES_FUNCTIONS,
            QUERY_FUNCTIONS,
            FUNCTION,
            FUNCTION_FORM,
            FUNCTION_DESCRIPTION,
            QUERY,
            QUERY_ADQL,
            QUERY_DESCRIPTION,
            CONTACT,
            CONTACT_NAME,
            CONTACT_URL,
            PUBLISHER,
            PUBLISHER_NAME,
            PUBLISHER_URL,
            UNSUPPORTED
        }

        /** Regular expression for the string serialisation of a Boolean value. */
        private final Pattern PATTERN_BOOLEAN = Pattern.compile("^(true|false)$");

        /** List of the already parsed Validation Queries and set metadata. */
        public final ValidationSet queries = new ValidationSet();

        /** Used to detect multiple/inner <code>&lt;queries&gt;</code> elements.
         * Set to true when the first (supposedly the root) one is met. */
        private boolean hasQueriesElt = false;

        /** Context stack. The head is the current context. */
        private final Stack<Context> ctx = new Stack<>();

        /** Parser position inside the XML document. */
        private Locator loc = null;

        /**  */
        private UDF func = null;

        /** The Validation Query being parsed. */
        private ValidationQuery q = null;

        /** Text content of the current XML tag. */
        private final StringBuffer eltContent = new StringBuffer();

        @Override
        public void setDocumentLocator(final Locator locator) {
            super.setDocumentLocator(locator);
            loc = locator;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            // Reset the last read element text value:
            eltContent.delete(0, eltContent.length());

            // Case-insensitive:
            qName = qName.toLowerCase();

            /*
             * QUERIES SET
             */
            if (qName.equals("queries")) {
                if (hasQueriesElt)
                    throw new SAXParseException("Inner queries suites not supported!", loc);
                hasQueriesElt = true;
                ctx.push(Context.QUERIES);
            }
            /*
             * QUERIES TITLE
             */
            else if (qName.equals("title")) {
                ctx.push(Context.QUERIES_TITLE);
            }
            /*
             * CONTACT
             */
            else if (qName.equals("contact")) {
                queries.contact = new Contact();
                ctx.push(Context.CONTACT);
            }
            /*
             * PUBLISHER
             */
            else if (qName.equals("publisher")) {
                queries.publisher = new Publisher();
                ctx.push(Context.PUBLISHER);
            }
            /*
             * CONTACT/PUBLISHER NAME
             */
            else if (qName.equals("name")) {
                switch (ctx.peek()) {
                    case CONTACT:
                        ctx.push(Context.CONTACT_NAME);
                        break;
                    case PUBLISHER:
                        ctx.push(Context.PUBLISHER_NAME);
                        break;
                    default:
                        ctx.push(Context.UNSUPPORTED);
                        warnUnexpected(qName);
                }
            }
            /*
             * CONTACT/PUBLISHER URL
             */
            else if (qName.equals("url")) {
                    switch(ctx.peek()){
                        case CONTACT:
                            ctx.push(Context.CONTACT_URL);
                            break;
                        case PUBLISHER:
                            ctx.push(Context.PUBLISHER_URL);
                            break;
                        default:
                            ctx.push(Context.UNSUPPORTED);
                            warnUnexpected(qName);
                    }
            }
            /*
             * FUNCTIONS PARENT
             */
            else if (qName.equals("functions")) {
                switch(ctx.peek()){
                    case QUERIES:
                        func = new UDF();
                        ctx.push(Context.QUERIES_FUNCTIONS);
                        break;
                    case QUERY:
                        func = new UDF();
                        ctx.push(Context.QUERY_FUNCTIONS);
                        break;
                    default:
                        ctx.push(Context.UNSUPPORTED);
                        warnUnexpected(qName);
                }
            }
            /*
             * FUNCTION PARENT
             */
            else if (qName.equals("function")) {
                switch(ctx.peek()){
                    case QUERIES_FUNCTIONS:
                    case QUERY_FUNCTIONS:
                        func = new UDF();
                        ctx.push(Context.FUNCTION);
                        break;
                    default:
                        ctx.push(Context.UNSUPPORTED);
                        warnUnexpected(qName);
                }
            }
            /*
             * FUNCTION FORM/SIGNATURE
             */
            else if (qName.equals("form")) {
                if (ctx.peek() == Context.FUNCTION)
                    ctx.push(Context.FUNCTION_FORM);
                else{
                    ctx.push(Context.UNSUPPORTED);
                    warnUnexpected(qName);
                }
            }
            /*
             * QUERY PARENT
             */
            else if (qName.equals("query")) {
                String attrValue = attributes.getValue("uuid");
                q = new ValidationQuery(attrValue);
                if (attrValue == null)
                    info("Omitted value for the attribute 'uuid'. Automatically generated and set to: '"+q.id+"'.");
                ctx.push(Context.QUERY);
            }
            /*
             * QUERIES/QUERY/FUNCTION DESCRIPTION
             */
            else if (qName.equals("description")) {
                switch(ctx.peek()){
                    case QUERIES:
                        ctx.push(Context.QUERIES_DESCRIPTION);
                        break;
                    case QUERY:
                        ctx.push(Context.QUERY_DESCRIPTION);
                        break;
                    case FUNCTION:
                        ctx.push(Context.FUNCTION_DESCRIPTION);
                        break;
                    default:
                        ctx.push(Context.UNSUPPORTED);
                        warnUnexpected(qName);
                }
            }
            /*
             * THE ADQL QUERY ITSELF
             */
            else if (qName.equalsIgnoreCase("adql") && ctx.peek() == Context.QUERY)
            {
                // set the expected query validity:
                String attrValue = attributes.getValue("valid");
                q.isValid = Boolean.parseBoolean(attrValue);
                if (attrValue == null)
                    info("Omitted value for the attribute 'valid'. Set by default to: 'false'.");
                else if (!PATTERN_BOOLEAN.matcher(attrValue.toLowerCase()).find())
                    warning("Unsupported value for the attribute 'valid': '"+attrValue+"'! Set by default to: '"+q.isValid+"'.");

                // set the desired ADQL version:
                attrValue = attributes.getValue("version");
                q.adqlVersion = ValidationSetParser.parseVersion(attrValue);
                if (attrValue == null)
                    info("Omitted value for the attribute 'version'. Set by default to: '"+q.adqlVersion+"'.");
                else if (!ValidationSetParser.matchesVersion(attrValue))
                    warning("Unsupported value for the attribute 'version': '"+attrValue+"'! Set by default to: '"+q.adqlVersion+"'.");

                // change parsing context:
                ctx.push(Context.QUERY_ADQL);
            }
            /*
             * ANYTHING ELSE IS UNSUPPORTED!
             */
            else {
                if (ctx.empty())
                    throw new SAXParseException("Unsupported XML root tag: <" + qName + ">! Expected: <queries>.", loc);
                else
                {
                    ctx.push(Context.UNSUPPORTED);
                    warnUnexpected(qName);
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            final String content = eltContent.toString().trim();
            switch(ctx.pop()){
                case QUERIES:
                    if (queries.queries.isEmpty())
                        info("No query to validate in this validation set!");
                    break;
                case QUERIES_TITLE:
                    queries.title = content;
                    break;
                case QUERIES_DESCRIPTION:
                    queries.description = content;
                    break;
                case QUERY_DESCRIPTION:
                    q.description = content;
                    break;
                case FUNCTION_DESCRIPTION:
                    func.description = content;
                    break;
                case FUNCTION_FORM:
                    try {
                        func.setForm(content);
                    }catch(adql.parser.grammar.ParseException pe){
                        error("UDF ignored! Incorrect UDF definition: '"+content+"'. Cause: "+pe.getMessage());
                    }
                    break;
                case FUNCTION:
                    if (func.getForm() == null)
                        warning("Empty UDF definition ignored!");
                    else {
                        switch (ctx.peek()) {
                            case QUERIES_FUNCTIONS:
                                queries.functions.add(func);
                                break;
                            case QUERY_FUNCTIONS:
                                q.functions.add(func);
                                break;
                            default:
                                warning("Unused function definition: '(" + func.getForm() + ")'!");
                        }
                    }
                    func = null;
                    break;
                case QUERY_ADQL:
                    q.query = content;
                    break;
                case QUERY:
                    if (q.query != null && q.query.trim().length() > 0)
                        queries.queries.add(q);
                    else
                        warning("Missing ADQL query for the validation query '"+q.id+"'!");
                    q = null;
                    break;
                case CONTACT:
                    // If no info about the contact discard the object:
                    if ((queries.contact.name == null || queries.contact.name.trim().length() == 0) && (queries.contact.url == null))
                        queries.contact = null;
                    break;
                case CONTACT_NAME:
                    queries.contact.name = content;
                    break;
                case CONTACT_URL:
                    try {
                        queries.contact.url = new URL(content);
                    } catch (MalformedURLException e) {
                        warning("Malformed contact's URL: '"+content+"'! Cause: "+e.getMessage());
                    }
                    break;
                case PUBLISHER:
                    // If no info about the publisher discard the object:
                    if ((queries.publisher.name == null || queries.publisher.name.trim().length() == 0) && (queries.publisher.url == null))
                        queries.publisher = null;
                    break;
                case PUBLISHER_NAME:
                    queries.publisher.name = content;
                    break;
                case PUBLISHER_URL:
                    try {
                        queries.publisher.url = new URL(content);
                    } catch (MalformedURLException e) {
                        warning("Malformed publisher's URL: '"+content+"'! Cause: "+e.getMessage());
                    }
                    break;
            }
            eltContent.delete(0, eltContent.length());
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (ctx.empty())
                throw new SAXParseException("Unsupported ADQL validation set format! Expected: XML document.", loc);
            else
                eltContent.append(System.lineSeparator()).append(new String(ch, start, length));
        }

        @Override
        public void warning(SAXParseException e) {
            warning("- SAXParseException: "+e.getMessage());
        }

        @Override
        public void error(SAXParseException e) {
            LOGGER.severe("[l." + loc.getLineNumber() + ", c." + loc.getColumnNumber() + "] - SAXParseException: "+e.getMessage());
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            LOGGER.severe("[l." + loc.getLineNumber() + ", c." + loc.getColumnNumber() + "] - SAXParseException: FATAL: "+e.getMessage());
            super.fatalError(e);
        }

        /**
         * Log an information message with the current parser location.
         *
         * @param message The message to log.
         */
        protected void info(final String message) {
            LOGGER.info("[l." + loc.getLineNumber() + ", c." + loc.getColumnNumber() + "] "+message);
        }

        /**
         * Log a warning message with the current parser location.
         *
         * @param message The message to log.
         */
        protected void warning(final String message) {
            LOGGER.warning("[l." + loc.getLineNumber() + ", c." + loc.getColumnNumber() + "] "+message);
        }

        /**
         * Log an error message with the current parser location.
         *
         * @param message The message to log.
         */
        protected void error(final String message) {
            LOGGER.severe("[l." + loc.getLineNumber() + ", c." + loc.getColumnNumber() + "] "+message);
        }

        /**
         * Log a warning message stating that the given XML node's name was NOT
         * expected.
         *
         * @param qName Name of the unexpected XML tag.
         */
        protected void warnUnexpected(final String qName) {
            warning("Unexpected <"+qName+"> element!");
        }
    }

}
