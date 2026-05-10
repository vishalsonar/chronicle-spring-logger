package io.github.vishalsonar.csl.configuration;

import io.github.vishalsonar.csl.service.AppenderService;
import io.github.vishalsonar.csl.service.LogConsumerService;
import io.github.vishalsonar.csl.service.LogFileWriterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.openhft.chronicle.queue.ChronicleQueue;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@Slf4j
@AutoConfiguration
@RequiredArgsConstructor
@ConditionalOnClass(name = "net.openhft.chronicle.queue.ChronicleQueue")
@ConditionalOnProperty(
        prefix = "chronicle-spring-logger",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Import({
        LogFileWriterService.class,
        LogConsumerService.class
})
public class AppenderAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AppenderService appenderService() {
        return new AppenderService();
    }

    @Bean(name = "chronicleLogQueue", destroyMethod = "close")
    public ChronicleQueue chronicleLogQueue() {
        return AppenderService.getQueue();
    }
}
