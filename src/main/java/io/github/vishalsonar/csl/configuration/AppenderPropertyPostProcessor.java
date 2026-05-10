package io.github.vishalsonar.csl.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.ResourcePropertySource;

import java.io.IOException;

@Slf4j
public class AppenderPropertyPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        try {
            ClassPathResource resource = new ClassPathResource("chronicle-spring-logger.properties");
            if (resource.exists()) {
                environment.getPropertySources().addLast(new ResourcePropertySource(resource));
            }
        } catch (IOException ioException) {
            log.error("[ChronicleLogger] Failed to import chronicle-spring-logger.properties: {}", ioException);
        }
    }
}