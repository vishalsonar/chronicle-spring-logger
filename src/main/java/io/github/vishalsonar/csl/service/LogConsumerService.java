package io.github.vishalsonar.csl.service;

import io.github.vishalsonar.csl.configuration.AppenderProperties;
import io.github.vishalsonar.csl.event.LogEvent;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptTailer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
public class LogConsumerService {

    @Qualifier("chronicleLogQueue")
    private final ChronicleQueue chronicleLogQueue;

    private final LogFileWriterService fileWriter;
    private final AppenderProperties properties;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ExecutorService executor;

    @PostConstruct
    public void start() {
        running.set(true);
        executor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, properties.getConsumerThreadName());
            thread.setDaemon(true);
            thread.setPriority(Thread.MIN_PRIORITY);
            return thread;
        });
        executor.submit(this::drainLoop);
    }

    private void drainLoop() {
        var tailer = chronicleLogQueue.createTailer("log-consumer");
        while (running.get() && !Thread.currentThread().isInterrupted()) {
            try {
                if (!tryReadOne(tailer)) {
                    Thread.sleep(properties.getPollIntervalMs());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                System.err.println("[ChronicleLogger] Consumer error: " + e.getMessage());
            }
        }
        flushRemaining(tailer);
    }

    private boolean tryReadOne(ExcerptTailer tailer) {
        try (var documentContext = tailer.readingDocument()) {
            if (!documentContext.isPresent()) return false;
            try {
                var event = new LogEvent();
                event.readMarshallable(documentContext.wire());
                if (event.getMessage() == null) {
                    System.err.println("[chronicle-spring-logger] read null message, skipping.");
                    return true;
                }
                fileWriter.write(event);
                return true;
            } catch (Exception e) {
                documentContext.rollbackOnClose();
                System.err.println("[chronicle-spring-logger] readingDocument error: " + e.getMessage());
                return false;
            }
        }
    }

    private void flushRemaining(ExcerptTailer tailer) {
        System.out.println("[ChronicleLogger] Flushing remaining logs before shutdown...");
        while (tryReadOne(tailer)) {
        }
    }

    @PreDestroy
    public void stop() {
        running.set(false);
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
