package io.github.vishalsonar.csl.service;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.AppenderBase;
import io.github.vishalsonar.csl.event.LogEvent;
import lombok.extern.slf4j.Slf4j;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.wire.DocumentContext;

@Slf4j
public class AppenderService extends AppenderBase<ILoggingEvent> {

    private static ChronicleQueue chronicleQueue;

    public static void setQueue(ChronicleQueue chronicleQueue) {
        AppenderService.chronicleQueue = chronicleQueue;
    }

    public static ChronicleQueue getQueue() {
        return AppenderService.chronicleQueue;
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (chronicleQueue == null || !isStarted()) return;
        try (DocumentContext documentContext = chronicleQueue.createAppender().writingDocument()) {
            toLogEvent(event).writeMarshallable(documentContext.wire());
        } catch (Exception exception) {
            log.error("[ChronicleLogger] Failed to append log event: {}", exception);
        }
    }

    private LogEvent toLogEvent(ILoggingEvent event) {
        IThrowableProxy throwableProxy = event.getThrowableProxy();
        String exceptionMessage = (throwableProxy != null) ? throwableProxy.getClassName() + ": " + throwableProxy.getMessage() : null;
        return new LogEvent()
                .setTimestamp(event.getTimeStamp())
                .setLevel(event.getLevel().toString())
                .setLogger(event.getLoggerName())
                .setThread(event.getThreadName())
                .setProcessId(ProcessHandle.current().pid())
                .setMessage(event.getFormattedMessage())
                .setException(exceptionMessage);
    }
}
