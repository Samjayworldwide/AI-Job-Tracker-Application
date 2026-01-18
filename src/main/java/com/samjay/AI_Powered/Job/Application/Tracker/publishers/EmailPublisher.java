package com.samjay.AI_Powered.Job.Application.Tracker.publishers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samjay.AI_Powered.Job.Application.Tracker.dtos.request.EmailRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Sender;

import static com.samjay.AI_Powered.Job.Application.Tracker.utils.Utility.EMAIL_QUEUE;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailPublisher {

    private final Sender sender;

    private final ObjectMapper objectMapper;


    public Mono<Void> queueEmail(EmailRequestDto emailRequestDto) {

        return Mono.fromCallable(() -> objectMapper.writeValueAsBytes(emailRequestDto))
                .flatMap(bytes -> {

                    Flux<OutboundMessage> outbound = Flux.just(new OutboundMessage("", EMAIL_QUEUE, bytes));

                    return sender.sendWithPublishConfirms(outbound)
                            .doOnNext(result -> {

                                if (result.isAck())
                                    log.info("Email queued successfully for: {}", emailRequestDto.recipient());

                                else
                                    log.error("Email failed to queue for: {}", emailRequestDto.recipient());
                            })
                            .doOnError(e -> log.error("Error queueing email", e))
                            .then();
                })
                .onErrorResume(JsonProcessingException.class, e -> {

                    log.error("Failed to serialize email details", e);

                    return Mono.empty();

                });
    }
}