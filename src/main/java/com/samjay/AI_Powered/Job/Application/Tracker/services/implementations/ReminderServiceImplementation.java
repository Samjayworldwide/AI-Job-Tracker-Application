package com.samjay.AI_Powered.Job.Application.Tracker.services.implementations;

import com.samjay.AI_Powered.Job.Application.Tracker.entities.Reminder;
import com.samjay.AI_Powered.Job.Application.Tracker.repositories.ReminderRepository;
import com.samjay.AI_Powered.Job.Application.Tracker.services.ReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderServiceImplementation implements ReminderService {

    private final ReminderRepository reminderRepository;

    @Override
    public Mono<Void> createReminder(String applicationId, LocalDate remindAt) {

        return Mono.defer(() -> {

                    Reminder reminder = Reminder.builder()
                            .id(UUID.randomUUID().toString())
                            .applicationId(applicationId)
                            .reminderDate(remindAt)
                            .isSent(false)
                            .build();

                    return reminderRepository.save(reminder);
                })
                .doOnSuccess(r -> log.info("Reminder created successfully for applicationId: {}", applicationId))
                .then();
    }
}