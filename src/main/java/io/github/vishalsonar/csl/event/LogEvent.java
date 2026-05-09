package io.github.vishalsonar.csl.event;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.openhft.chronicle.wire.Marshallable;
import net.openhft.chronicle.wire.WireIn;
import net.openhft.chronicle.wire.WireOut;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
public class LogEvent implements Marshallable {

    private String level;
    private String logger;
    private String thread;
    private long processId;
    private String message;
    private String exception;
    private long timestamp;

    public LogEvent(long timestamp, String level, String logger, String thread, long processId, String message, String exception) {
        this.timestamp = timestamp;
        this.level = level;
        this.logger = logger;
        this.thread = thread;
        this.processId = processId;
        this.message = message;
        this.exception = exception;
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
