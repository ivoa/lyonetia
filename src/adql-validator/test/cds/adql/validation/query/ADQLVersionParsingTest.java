package cds.adql.validation.query;

import static adql.parser.ADQLParser.ADQLVersion;
import static org.junit.jupiter.api.Assertions.*;

import cds.adql.validation.parser.ValidationSetParser;
import org.junit.jupiter.api.Test;

public class ADQLVersionParsingTest {

    final String[] CORRECT_2_0 = new String[]{"2.0", "v2.0", "V2.0", "adql-2.0", "adql-v2.0", "Adql-V2.0", "ADQL-2.0"};
    final String[] CORRECT_2_1 = new String[]{"2.1", "2", "v2", "v2.1", "V2", "V2.1", "adql-2.1", "adql-v2.1", "adql-v2", "Adql-V2", "ADQL-2.1", "ADQL-v2.1"};

    @Test
    public void parseEmpty() {
        for(String s : new String[]{null, "", "  ", " \t\n "})
            assertEquals(ValidationSetParser.DEFAULT_ADQL_VERSION, ValidationSetParser.parseVersion(s));
    }

    @Test
    public void parse2_0(){
        for(String s : CORRECT_2_0)
            assertEquals(ADQLVersion.V2_0, ValidationSetParser.parseVersion(s));
    }

    @Test
    public void parse2_1(){
        for(String s : CORRECT_2_1)
            assertEquals(ADQLVersion.V2_1, ValidationSetParser.parseVersion(s));
    }

    @Test
    public void matchesVersion_Null_Or_Empty(){
        assertFalse(ValidationSetParser.matchesVersion(null));
        assertFalse(ValidationSetParser.matchesVersion(""));
        assertFalse(ValidationSetParser.matchesVersion("     "));
    }

    @Test
    public void matchesVersion_2_0(){
        for(String str : CORRECT_2_0)
            assertTrue(ValidationSetParser.matchesVersion(str));
    }

    @Test
    public void matchesVersion_2_1(){
        for(String str : CORRECT_2_1)
            assertTrue(ValidationSetParser.matchesVersion(str));
    }

    @Test
    public void matchesVersion_Not_A_Version(){
        for(String str : new String[]{"foo", ".2.1"})
            assertFalse(ValidationSetParser.matchesVersion(str));
    }

}