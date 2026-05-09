package io.github.vishalsonar.csl.service;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.AppenderBase;
import io.github.vishalsonar.csl.event.LogEvent;
import lombok.extern.slf4j.Slf4j;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.wire.DocumentContext;

@Slf4j
public class AppenderService extends AppenderBase<ILoggingEvent> {

    private static volatile ChronicleQueue chronicleQueue;
    private static final ThreadLocal<ExcerptAppender> excerptAppenderThreadLocal = new ThreadLocal<>();

    public static void setQueue(ChronicleQueue chronicleQueue) {
        AppenderService.chronicleQueue = chronicleQueue;
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (chronicleQueue == null || !isStarted()) return;
        try (DocumentContext documentContext = getOrCreateAppender().writingDocument()) {
            toLogEvent(event).writeMarshallable(documentContext.wire());
        } catch (Exception e) {
            log.error("[ChronicleLogger] Failed to append log event: {}", e);
        }
    }

    private ExcerptAppender getOrCreateAppender() {
        ExcerptAppender appender = excerptAppenderThreadLocal.get();
        if (appender == null) {
            excerptAppenderThreadLocal.set(appender = chronicleQueue.createAppender());
        }
        return appender;
    }

    private LogEvent toLogEvent(ILoggingEvent event) {
        IThrowableProxy throwableProxy = event.getThrowableProxy();
        String exceptionMessage = (throwableProxy != null) ? throwableProxy.getClassName() + ": " + throwableProxy.getMessage() : null;
        return new LogEvent(
                event.getTimeStamp(),
                event.getLevel().toString(),
                event.getLoggerName(),
                event.getThreadName(),
                ProcessHandle.current().pid(),
                event.getFormattedMessage(),
                exceptionMessage
        );
    }
}
