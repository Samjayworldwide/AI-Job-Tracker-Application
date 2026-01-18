package com.samjay.AI_Powered.Job.Application.Tracker.producers;

import reactor.core.publisher.Mono;

public interface KafkaProducerService {

    Mono<Void> sendMessageAsync(String topic, String key, Object message);

}