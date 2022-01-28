package cds.adql.validation.report;

import cds.adql.validation.ValidationException;
import cds.adql.validation.query.ValidationQuery;
import cds.adql.validation.query.ValidationSet;

public class RecorderListener extends VoidListener {

    int cntPass = 0;
    int cntFail = 0;

    boolean setStarted = false;
    boolean setEnded = false;
    boolean queryStarted = false;
    boolean queryEnded = false;

    final StringBuffer errors   = new StringBuffer();
    final StringBuffer failures = new StringBuffer();

    @Override
    public void clear() {
        cntPass = 0;
        cntFail = 0;

        setStarted   = false;
        setEnded     = false;
        queryStarted = false;
        queryEnded   = false;

        errors.delete(0, errors.length());
        failures.delete(0, failures.length());
    }

    @Override
    public void error(ValidationException error) {
        if (error != null) {
            // One error per line:
            if (errors.length() > 0)
                errors.append(System.lineSeparator());

            // Append the error:
            errors.append(error.getMessage());
        }
    }

    public String getErrors(){
        return errors.toString();
    }

    public String getFailures(){
        return failures.toString();
    }

    public int getCntPass() {
        return cntPass;
    }

    public int getCntFail() {
        return cntFail;
    }

    public boolean isSetStarted() {
        return setStarted;
    }

    public boolean isSetEnded() {
        return setEnded;
    }

    public boolean isQueryStarted() {
        return queryStarted;
    }

    public boolean isQueryEnded() {
        return queryEnded;
    }

    @Override
    public void start(ValidationSet set, String source) {
        setStarted   = true;
        setEnded     = false;
        queryStarted = false;
        queryEnded   = false;
    }

    @Override
    public void validating(ValidationQuery query) {
        queryStarted = true;
        queryEnded   = false;
    }

    @Override
    public void pass(ValidationQuery query) {
        queryEnded   = true;
        cntPass++;
    }

    @Override
    public void fail(ValidationQuery query, String errorMessage) {
        queryEnded   = true;
        cntFail++;

        if (errorMessage != null) {
            // One failure per line:
            if (failures.length() > 0)
                failures.append(System.lineSeparator());

            // Append the failure:
            failures.append(errorMessage);
        }
    }

    @Override
    public void end(ValidationSet set) {
        setEnded     = true;
    }
}
