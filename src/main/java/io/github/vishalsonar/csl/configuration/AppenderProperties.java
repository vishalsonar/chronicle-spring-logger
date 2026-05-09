package io.github.vishalsonar.csl.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "chronicle-spring-logger")
public class AppenderProperties {

    // Directory where Chronicle Queue roll files are stored 
    private String queuePath = "data/log-queue";

    // Chronicle Queue roll cycle: DAILY | HOURLY | MINUTELY 
    private String rollCycle = "DAILY";

    // How often the consumer thread polls the queue when idle (ms) 
    private int pollIntervalMs = 5;

    // Name of the background consumer thread 
    private String consumerThreadName = "chronicle-log-consumer";

    // Output log file path 
    private String logFilePath = "logs/app.log";

    // How many days of log roll files to retain 
    private int maxHistoryDays = 30;

    // Max total size of log files 
    private String totalSizeCap = "1GB";

    // Log pattern for file output 
    private String logPattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level %X{PID} --- [%20X{THREAD_NAME}] %X{LOGGER_NAME} : %msg%n";
}
