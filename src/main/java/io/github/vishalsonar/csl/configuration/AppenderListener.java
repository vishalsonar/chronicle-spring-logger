package io.github.vishalsonar.csl.configuration;

import ch.qos.logback.classic.LoggerContext;
import io.github.vishalsonar.csl.service.AppenderService;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.RollCycles;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;

import java.io.File;

public class AppenderListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        AppenderService.setQueue(createQueue(event));
        AppenderService appender = new AppenderService();
        appender.setContext(context);
        appender.setName("CHRONICLE_AUTO");
        appender.start();
        context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).addAppender(appender);
    }

    private ChronicleQueue createQueue(ApplicationEnvironmentPreparedEvent event) {
        String queuePath = event.getEnvironment().getProperty("chronicle-spring-logger.queuePath", "data/log-queue");
        String rollCycle = event.getEnvironment().getProperty("chronicle-spring-logger.rollCycle", "DAILY");
        new File(queuePath).mkdirs();
        return SingleChronicleQueueBuilder.binary(queuePath).rollCycle(parseRollCycle(rollCycle)).build();
    }

    private RollCycles parseRollCycle(String value) {
        if (value == null) return RollCycles.FAST_DAILY;
        return switch (value.toUpperCase()) {
            case "HOURLY" -> RollCycles.FAST_HOURLY;
            case "MINUTELY" -> RollCycles.TEN_MINUTELY;
            default -> RollCycles.FAST_DAILY;
        };
    }
}
