package io.github.vishalsonar.csl.service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.openhft.chronicle.queue.ChronicleQueue;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Spliterators;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppenderInitializerService {

    @Qualifier("chronicleLogQueue")
    private final ChronicleQueue chronicleLogQueue;

    @PostConstruct
    public void init() {
        AppenderService.setQueue(chronicleLogQueue);
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        boolean alreadyRegistered = StreamSupport.stream(Spliterators.spliteratorUnknownSize(rootLogger.iteratorForAppenders(), 0), false)
                                                 .anyMatch(AppenderService.class::isInstance);
        if (alreadyRegistered) {
            log.info("[ChronicleLogger] Already registered via logback-spring.xml.");
            return;
        }
        registerAutoAppender(rootLogger, context);
    }

    private void registerAutoAppender(Logger rootLogger, LoggerContext context) {
        var appender = new AppenderService();
        appender.setContext(context);
        appender.setName("CHRONICLE_AUTO");
        appender.start();
        rootLogger.addAppender(appender);
        log.info("[ChronicleLogger] Auto-registered on root logger.");
    }
}
