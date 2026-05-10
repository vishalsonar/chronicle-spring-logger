package io.github.vishalsonar.csl.event;

import net.openhft.chronicle.wire.Marshallable;
import net.openhft.chronicle.wire.WireIn;
import net.openhft.chronicle.wire.WireOut;
import org.jetbrains.annotations.NotNull;

public class LogEvent implements Marshallable {

    private String level;
    private String logger;
    private String thread;
    private long processId;
    private String message;
    private String exception;
    private long timestamp;

    public String getLevel() {
        return level;
    }

    public LogEvent setLevel(String level) {
        this.level = level;
        return this;
    }

    public String getLogger() {
        return logger;
    }

    public LogEvent setLogger(String logger) {
        this.logger = logger;
        return this;
    }

    public String getThread() {
        return thread;
    }

    public LogEvent setThread(String thread) {
        this.thread = thread;
        return this;
    }

    public long getProcessId() {
        return processId;
    }

    public LogEvent setProcessId(long processId) {
        this.processId = processId;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public LogEvent setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getException() {
        return exception;
    }

    public LogEvent setException(String exception) {
        this.exception = exception;
        return this;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public LogEvent setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    @Override
    public void writeMarshallable(@NotNull WireOut wire) {
        wire.write("timestamp").int64(timestamp)
                .write("level").text(level)
                .write("logger").text(logger)
                .write("thread").text(thread)
                .write("processId").int64(processId)
                .write("message").text(message)
                .write("exception").text(exception);
    }

    @Override
    public void readMarshallable(@NotNull WireIn wire) {
        timestamp = wire.read("timestamp").int64();
        level = wire.read("level").text();
        logger = wire.read("logger").text();
        thread = wire.read("thread").text();
        processId = wire.read("processId").int64();
        message = wire.read("message").text();
        exception = wire.read("exception").text();
    }
}
