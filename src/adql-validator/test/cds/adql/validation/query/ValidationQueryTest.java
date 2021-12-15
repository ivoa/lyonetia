package cds.adql.validation.query;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ValidationQueryTest {

    @Test
    void testEquals_With_Null() {
        final ValidationQuery q = new ValidationQuery();
        assertFalse(q.equals(null));
    }

    @Test
    void testEquals_With_Another_Class() {
        final ValidationQuery q = new ValidationQuery();
        assertFalse(q.equals("Hello"));
    }

    @Test
    void testEquals_Same_Object() {
        final ValidationQuery q = new ValidationQuery();
        assertTrue(q.equals(q));
    }

    @Test
    void testEquals_Same_Id() {
        final UUID ID = UUID.randomUUID();
        final ValidationQuery q1 = new ValidationQuery(ID);
        final ValidationQuery q2 = new ValidationQuery(ID);
        assertTrue(q1.equals(q2));
    }

    @Test
    void testEquals_Different_Id() {
        final ValidationQuery q1 = new ValidationQuery(UUID.randomUUID());
        final ValidationQuery q2 = new ValidationQuery(UUID.randomUUID());
        assertFalse(q1.equals(q2));
    }
}