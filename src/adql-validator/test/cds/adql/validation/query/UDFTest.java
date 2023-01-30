package cds.adql.validation.query;

import adql.parser.grammar.ParseException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UDFTest {

    @Test
    void setForm_NULL() {
        final UDF udf = new UDF();
        assertDoesNotThrow(() -> udf.setForm(null));
        assertNull(udf.getForm());
        assertNull(udf.getFeature());
    }

    @Test
    void setForm_Empty(){
        final UDF udf = new UDF();
        for(String str : new String[]{"", " ", "\t", "    \t  \n  "}) {
            assertDoesNotThrow(() -> udf.setForm(str));
            assertNull(udf.getForm());
            assertNull(udf.getFeature());
        }
    }

    @Test
    void setForm_IncorrectFormat(){
        final UDF udf = new UDF();
        final ParseException pe = assertThrows(ParseException.class, () -> udf.setForm("toto"));
        assertEquals("Wrong function definition syntax! Expected syntax: \"<regular_identifier>(<parameters>?) <return_type>?\", where <regular_identifier>=\"[a-zA-Z]+[a-zA-Z0-9_]*\", <return_type>=\" -> <type_name>\", <parameters>=\"(<regular_identifier> <type_name> (, <regular_identifier> <type_name>)*)\", <type_name> should be one of the types described in the UPLOAD section of the TAP documentation. Examples of good syntax: \"foo()\", \"foo() -> VARCHAR\", \"foo(param INTEGER)\", \"foo(param1 INTEGER, param2 DOUBLE) -> DOUBLE\"", pe.getMessage());
        assertNull(udf.getForm());
        assertNull(udf.getFeature());
    }

    @Test
    void setForm_CorrectFormat(){
        final UDF udf = new UDF();
        final String FORM = "ivo_healpix_index(hpxOrder INTEGER, long REAL, lat REAL) -> BIGINT";
        assertDoesNotThrow(() -> udf.setForm(FORM));
        assertEquals(FORM, udf.getForm());
        assertNotNull(udf.getFeature());
        assertEquals(FORM, udf.getFeature().form);
    }

    @Test
    void setForm_Duplicated_Space_Characters(){
        final UDF udf = new UDF();
        final String EXPECTED_FORM = "ivo_healpix_index(hpxOrder INTEGER, long REAL, lat REAL) -> BIGINT";
        final String FORM = "  \t ivo_healpix_index(hpxOrder   INTEGER, \t long REAL,  \n\t lat REAL)    \n\t->    BIGINT  ";
        assertDoesNotThrow(() -> udf.setForm(FORM));
        assertEquals(EXPECTED_FORM, udf.getForm());
    }
}