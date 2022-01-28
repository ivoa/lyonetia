package cds.adql.validation.report;

import cds.adql.validation.ValidationException;
import cds.adql.validation.query.ValidationQuery;
import cds.adql.validation.query.ValidationSet;

public class VoidListener implements ValidatorListener {
    @Override
    public void clear() {

    }

    @Override
    public void start(ValidationSet set, String source) {

    }

    @Override
    public void validating(ValidationQuery query) {

    }

    @Override
    public void pass(ValidationQuery query) {

    }

    @Override
    public void fail(ValidationQuery query, String errorMessage) {

    }

    @Override
    public void end(ValidationSet set) {

    }

    @Override
    public void error(ValidationException error) {

    }

    @Override
    public boolean isShowOnlyFailures() {
        return false;
    }

    @Override
    public void setShowOnlyFailures(boolean showOnlyFailures) {

    }

    @Override
    public void setValidationStats(ValidationStatistics validationStats) {

    }
}
