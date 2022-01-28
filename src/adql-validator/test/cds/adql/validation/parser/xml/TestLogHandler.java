package cds.adql.validation.parser.xml;

import java.util.Iterator;
import java.util.Stack;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class TestLogHandler extends Handler implements Iterable<LogRecord> {

    private Stack<LogRecord> logs = new Stack<>();

    public void publish(final LogRecord record) {
        logs.push(record);
    }

    public Iterator<LogRecord> iterator() {
        return logs.iterator();
    }

    public int countLogs(){
        return logs.size();
    }

    @Override
    public void flush() {
        logs.clear();
    }

    @Override
    public void close() throws SecurityException {
        flush();
        logs = null;
    }

}
