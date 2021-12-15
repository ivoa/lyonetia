package cds.adql.validation.report;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StatCollectorTest {

    private void assertNoStats(final StatCollector coll){
        assertEquals(0, coll.getCntTests());
        assertEquals(0, coll.getCntPass());
        assertEquals(0, coll.getCntFail());
        assertEquals(0, coll.getTotalDuration());
        assertEquals(0, coll.getAvgDuration());
        assertEquals(0, coll.getLastTestDuration());
    }

    @Test
    public void testInit(){
        final StatCollector coll = new StatCollector();
        assertNoStats(coll);
    }

    @Test
    public void testEffectlessMethods(){
        final StatCollector coll = new StatCollector();

        // Starting a validation test set:
        coll.start(null, null);
        assertNoStats(coll);

        // Error:
        coll.error(null);
        assertNoStats(coll);

        // Ending a validation test set:
        coll.end(null);
        assertNoStats(coll);
    }

    @Test
    public void testStatsCollection() throws InterruptedException {
        final StatCollector coll = new StatCollector();

        // A very quick successful test query:
        coll.validating(null); // No need to provide a real query.
        coll.pass(null);       // (idem)
        // Change of the nb of Tests + Pass only:
        assertEquals(1, coll.getCntTests());
        assertEquals(1, coll.getCntPass());
        assertEquals(0, coll.getCntFail());
        // The last test duration should no longer be 0:
        assertTrue(coll.getLastTestDuration() > 0);
        // The total duration should be the same as the last test duration:
        assertEquals(coll.getLastTestDuration(), coll.getTotalDuration());
        // The average of only one test should be the same as the total duration:
        assertEquals(coll.getTotalDuration(), coll.getAvgDuration());

        // Keep the last total duration for the next tests:
        final long prevTotalDuration = coll.getTotalDuration();

        // A longer failed test query:
        coll.validating(null);
        Thread.sleep(1000L); // wait for 1s = 1000ms
        coll.fail(null, null);
        // Change of the nb of Tests + Fail only:
        assertEquals(2, coll.getCntTests());
        assertEquals(1, coll.getCntPass());
        assertEquals(1, coll.getCntFail());
        // The last test duration should not be 0 and should be greater than 1s:
        assertTrue(coll.getLastTestDuration() >= 1e9);
        // The total duration should have increased:
        assertTrue(coll.getTotalDuration() > 0);
        assertTrue(coll.getTotalDuration() > prevTotalDuration);
        // The total duration should be the same as before + lastTestDuration:
        assertEquals(prevTotalDuration + coll.getLastTestDuration(), coll.getTotalDuration());
        // This time the total duration should be divided by 2:
        assertEquals(coll.getTotalDuration()/2, coll.getAvgDuration());
    }

    @Test
    public void testEndAQueryNotStarted(){
        final StatCollector coll = new StatCollector();

        // Passed validation test not started:
        coll.pass(null);
        assertNoStats(coll);

        // Failed validation test not started:
        coll.fail(null, null);
        assertNoStats(coll);
    }

    @Test
    public void testReset(){
        final StatCollector coll = new StatCollector();

        // Simulate some test queries (1 failed + 1 passed to set all counters):
        coll.validating(null); // No need to provide a real query.
        coll.pass(null);       // (idem)
        coll.validating(null); // No need to provide a real query.
        coll.fail(null, null);       // (idem)
        // Stats not any more null:
        assertEquals(2, coll.getCntTests());
        assertEquals(1, coll.getCntPass());
        assertEquals(1, coll.getCntFail());
        assertTrue(coll.getTotalDuration() > 0);
        assertTrue(coll.getAvgDuration() > 0);
        assertTrue(coll.getLastTestDuration() > 0);

        // Reset should erase all counters:
        coll.clear();
        assertNoStats(coll);
    }

}