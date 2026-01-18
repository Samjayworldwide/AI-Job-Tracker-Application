package com.samjay.AI_Powered.Job.Application.Tracker.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Connection;
import com.samjay.AI_Powered.Job.Application.Tracker.dtos.request.EmailRequestDto;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.rabbitmq.Receiver;

import java.util.Objects;

import static com.samjay.AI_Powered.Job.Application.Tracker.utils.Utility.EMAIL_QUEUE;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailListener {

    private final Receiver receiver;

    private final ObjectMapper objectMapper;

    private final Mono<Connection> connectionMono;

    private final JavaMailSender javaMailSender;

    private Disposable subscription;

    @Value("${email.username}")
    private String emailSender;

    @PostConstruct
    public void startListening() {

        log.info("Starting email listener for queue: {}", EMAIL_QUEUE);

        subscription = receiver.consumeAutoAck(EMAIL_QUEUE)
                .flatMap(message -> {

                    log.info("Received email queue message: {}", new String(message.getBody()));

                    try {

                        EmailRequestDto emailRequestDto = objectMapper.readValue(message.getBody(), EmailRequestDto.class);

                        return sendEmail(emailRequestDto);

                    } catch (Exception e) {

                        log.error("Error deserializing email message", e);

                        return Mono.empty();

                    }
                })
                .doOnError(e -> log.error("Error processing email message", e))
                .subscribe();
    }

    private Mono<Void> sendEmail(EmailRequestDto emailRequestDto) {

        return Mono.fromRunnable(() -> {

                    try {

                        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

                        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

                        mimeMessageHelper.setFrom(emailSender);

                        mimeMessageHelper.setTo(emailRequestDto.recipient());

                        mimeMessageHelper.setText(emailRequestDto.messageBody(), true);

                        mimeMessageHelper.setSubject(emailRequestDto.subject());

                        javaMailSender.send(mimeMessage);

                        log.info("Email sent successfully to: {}", emailRequestDto.recipient());

                    } catch (MessagingException e) {

                        log.error("Failed to send email to: {}", emailRequestDto.recipient(), e);

                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    @PreDestroy
    public void stopListening() {

        log.info("Stopping email listener");

        if (subscription != null && !subscription.isDisposed()) {

            subscription.dispose();

        }

        try {

            Objects.requireNonNull(connectionMono.block()).close();

        } catch (Exception e) {

            log.error("Error closing RabbitMQ connection", e);

        }
    }
}