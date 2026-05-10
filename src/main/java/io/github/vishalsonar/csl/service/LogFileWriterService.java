package io.github.vishalsonar.csl.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.pattern.TargetLengthBasedClassNameAbbreviator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import io.github.vishalsonar.csl.event.LogEvent;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Service
public class LogFileWriterService {

    @Value("${chronicle-spring-logger.logFilePath}")
    private String logFilePath;

    @Value("${chronicle-spring-logger.logPattern}")
    private String logPattern;

    @Value("${chronicle-spring-logger.maxHistoryDays}")
    private int maxHistoryDays;

    @Value("${chronicle-spring-logger.totalSizeCap}")
    private String totalSizeCap;

    private ch.qos.logback.classic.Logger privateLogger;
    private RollingFileAppender<ILoggingEvent> fileAppender;

    @PostConstruct
    public void init() {
        try {
            LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
            Path logPath = Paths.get(logFilePath);
            Files.createDirectories(logPath.getParent());

            var encoder = new PatternLayoutEncoder();
            encoder.setContext(context);
            encoder.setPattern(logPattern);
            encoder.start();

            fileAppender = new RollingFileAppender<>();
            fileAppender.setContext(context);
            fileAppender.setName("CHRONICLE_FILE_WRITER");
            fileAppender.setFile(logFilePath);
            fileAppender.setEncoder(encoder);
            fileAppender.setAppend(true);

            var policy = new SizeAndTimeBasedRollingPolicy<ILoggingEvent>();
            policy.setContext(context);
            policy.setParent(fileAppender);
            policy.setMaxHistory(maxHistoryDays);
            policy.setTotalSizeCap(FileSize.valueOf(totalSizeCap));
            policy.setMaxFileSize(FileSize.valueOf("50MB"));
            policy.setFileNamePattern(logFilePath.replace(".log", "") + ".%d{yyyy-MM-dd}.%i.log");
            policy.start();

            fileAppender.setRollingPolicy(policy);
            fileAppender.start();
            if (!fileAppender.isStarted()) {
                throw new IllegalStateException("Logback RollingFileAppender failed to start");
            }
            privateLogger = context.getLogger("chronicle.internal.writer");
            privateLogger.setLevel(Level.ALL);
            privateLogger.setAdditive(false);
            System.out.println("[ChronicleLogger] File writer ready at:" + logPath.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("[ChronicleLogger] Critical failure initializing LogFileWriterService" + e.getMessage());
        }
    }

    public void write(LogEvent event) {
        if (event == null || event.getMessage() == null) return;
        if (fileAppender == null || !fileAppender.isStarted()) {
            System.err.println("[ChronicleLogger] Appender not ready. Dropping event: {}" + event.getMessage());
            return;
        }
        try {
            Level level = Level.toLevel(event.getLevel(), Level.INFO);
            String fullMessage = (event.getException() != null) ? event.getMessage() + " | exception=" + event.getException() : event.getMessage();
            var loggingEvent = new LoggingEvent(event.getLogger(), privateLogger, level, fullMessage, null, null);
            loggingEvent.setTimeStamp(event.getTimestamp());
            Map<String, String> mdcMap = Map.of(
                    "PID", String.valueOf(event.getProcessId()),
                    "LOGGER_NAME", new TargetLengthBasedClassNameAbbreviator(50).abbreviate(event.getLogger()),
                    "THREAD_NAME", new TargetLengthBasedClassNameAbbreviator(20).abbreviate(event.getThread())
            );
            loggingEvent.setMDCPropertyMap(mdcMap);
            fileAppender.doAppend(loggingEvent);
        } catch (Exception e) {
            System.err.println("[ChronicleLogger] Failed to write event to file" + e.getMessage());
        }
    }

    @PreDestroy
    public void shutdown() {
        if (fileAppender != null && fileAppender.isStarted()) {
            fileAppender.stop();
            System.out.println("[ChronicleLogger] File writer service stopped.");
        }
    }
}