package cds.adql.validation.parser;

import static adql.parser.ADQLParser.ADQLVersion;
import cds.adql.validation.query.ValidationSet;

import java.io.InputStream;
import java.util.regex.Pattern;

/**
 * Parsing API for sets of ADQL validation queries.
 *
 * <p><i><b>Implementation note:</b>
 *     This interface already provides utility functions to parse variants of
 *     an ADQL version (e.g. <code>v2.0</code>, <code>adql 2</code>):
 *     {@link #parseVersion(String)} to parse the version as a string,
 *     and {@link #matchesVersion(String)} to test whether a string is a valid
 *     ADQL version.
 * </i></p>
 *
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 1.0 (10/2021)
 */
public interface ValidationSetParser {

    /**
     * Parses a set of ADQL validation queries.
     *
     * @param stream    The serialized queries set to parse.
     *
     * @return  The object representation of this queries set.
     *
     * @throws ParseException   In case of parsing error.
     */
    ValidationSet parse(final InputStream stream) throws ParseException;

    /** ADQL version set by default when none is specified.
     * Generally it corresponds to the most recent ADQL version. */
    ADQLVersion DEFAULT_ADQL_VERSION = ADQLVersion.V2_1;

    /** Regular expression for the version 2.1 of ADQL. */
    Pattern PATTERN_ADQL_2_1 = Pattern.compile("^(adql-?)?v?2(.1)?$");

    /** Regular expression for the version 2.0 of ADQL. */
    Pattern PATTERN_ADQL_2_0 = Pattern.compile("^(adql-?)?v?2.0$");

    /**
     * Tell whether the given string represents an ADQL version.
     *
     * <p><i><b>Notes:</b>
     *     This function is case IN-sensitive. Many variants are tested: with
     *     or without <code>adql</code> prefix, <code>v</code> prefix, minor
     *     release number suffix, ...
     * </i></p>
     *
     * @param str   The string which may represent an ADQL version.
     *
     * @return  <code>true</code> if an ADQL version,
     *          <code>false</code> otherwise.
     *
     * @see #PATTERN_ADQL_2_0
     * @see #PATTERN_ADQL_2_1
     */
    static boolean matchesVersion(String str){
        if (str == null || str.trim().length() == 0)
            return false;

        // Normalize the input string:
        str = str.trim().toLowerCase();

        // Try to resolve a version:
        return PATTERN_ADQL_2_1.matcher(str).find()
                || PATTERN_ADQL_2_0.matcher(str).find();
    }

    /**
     * Parse the given ADQL version string serialization.
     *
     * <p><i><b>Notes:</b>
     *     This function is case IN-sensitive. Many variants are tested: with
     *     or without <code>adql</code> prefix, <code>v</code> prefix, minor
     *     release number suffix, ...
     * </i></p>
     *
     * @param str   The string serialization of an ADQL version.
     *
     * @return  The interpreted ADQL version,
     *          or {@link #DEFAULT_ADQL_VERSION} if none is provided or if the
     *          given string can not be resolved as an {@link ADQLVersion}.
     */
    static ADQLVersion parseVersion(String str){
        if (str == null || str.trim().length() == 0)
            return DEFAULT_ADQL_VERSION;

        // Normalize the input string:
        str = str.trim().toLowerCase();

        // Parse and resolve the version as much as possible:
        if (PATTERN_ADQL_2_1.matcher(str).find())
            return ADQLVersion.V2_1;
        else if (PATTERN_ADQL_2_0.matcher(str).find())
            return ADQLVersion.V2_0;
        else
            return DEFAULT_ADQL_VERSION;
    }

}
