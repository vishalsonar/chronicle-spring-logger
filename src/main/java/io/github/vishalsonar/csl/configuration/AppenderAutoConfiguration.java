package io.github.vishalsonar.csl.configuration;

import io.github.vishalsonar.csl.service.AppenderInitializerService;
import io.github.vishalsonar.csl.service.AppenderService;
import io.github.vishalsonar.csl.service.LogConsumerService;
import io.github.vishalsonar.csl.service.LogFileWriterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.RollCycles;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.io.File;

@Slf4j
@AutoConfiguration
@RequiredArgsConstructor
@EnableConfigurationProperties(AppenderProperties.class)
@ConditionalOnClass(name = "net.openhft.chronicle.queue.ChronicleQueue")
@ConditionalOnProperty(
        prefix = "chronicle-spring-logger",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Import({
        AppenderInitializerService.class,
        LogFileWriterService.class,
        LogConsumerService.class
})
public class AppenderAutoConfiguration {

    private final AppenderProperties properties;

    @Bean
    @ConditionalOnMissingBean
    public AppenderService appenderService() {
        return new AppenderService();
    }

    @Bean(name = "chronicleLogQueue", destroyMethod = "close")
    public ChronicleQueue chronicleLogQueue() {
        var queuePath = properties.getQueuePath();
        var rollCycle = parseRollCycle(properties.getRollCycle());
        new File(queuePath).mkdirs();
        log.info("[ChronicleLogger] Initializing queue at '{}' with cycle '{}'", queuePath, rollCycle);
        return SingleChronicleQueueBuilder.binary(queuePath).rollCycle(rollCycle).build();
    }

    private RollCycles parseRollCycle(String value) {
        if (value == null) return RollCycles.FAST_DAILY;
        return switch (value.toUpperCase()) {
            case "HOURLY"   -> RollCycles.FAST_HOURLY;
            case "MINUTELY" -> RollCycles.TEN_MINUTELY;
            default         -> RollCycles.FAST_DAILY;
        };
    }
}
