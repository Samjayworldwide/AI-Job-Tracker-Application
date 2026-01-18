package com.samjay.AI_Powered.Job.Application.Tracker.initializers;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.rabbitmq.QueueSpecification;
import reactor.rabbitmq.Sender;

import static com.samjay.AI_Powered.Job.Application.Tracker.utils.Utility.EMAIL_QUEUE;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailQueueInitializer {

    private final Sender sender;

    @PostConstruct
    public void declareQueue() {

        sender.declareQueue(QueueSpecification.queue(EMAIL_QUEUE).durable(true))
                .doOnSuccess(declareOk -> log.info("Queue '{}' declared successfully", EMAIL_QUEUE))
                .doOnError(e -> log.error("Failed to declare queue '{}'", EMAIL_QUEUE, e))
                .block();
    }
}