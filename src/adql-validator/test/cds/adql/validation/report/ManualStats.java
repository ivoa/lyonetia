package cds.adql.validation.report;

public class ManualStats implements ValidationStatistics {
    public long cntTests = 0;
    public long cntPass = 0;
    public long totalDuration = 0;
    public long lastTestDuration = 0;

    @Override
    public long getCntTests() {
        return cntTests;
    }

    @Override
    public long getCntPass() {
        return cntPass;
    }

    @Override
    public long getCntFail() {
        return cntTests - cntPass;
    }

    @Override
    public long getTotalDuration() {
        return totalDuration;
    }

    @Override
    public long getAvgDuration() {
        return (cntTests <= 0 ? 0 : totalDuration / cntTests);
    }

    @Override
    public long getLastTestDuration() {
        return lastTestDuration;
    }
}
