package com.samjay.AI_Powered.Job.Application.Tracker.producers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerServiceImplementation implements KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public Mono<Void> sendMessageAsync(String topic, String key, Object message) {

        return Mono.fromFuture(kafkaTemplate.send(topic, key, message))
                .doOnSuccess(result -> log.info("Kafka message sent to topic={}, key={}, partition={}, offset={}",
                        topic, key,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset())
                )
                .doOnError(ex -> log.error("Failed to send Kafka message to topic={}, key={}", topic, key, ex))
                .then();
    }
}