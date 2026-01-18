package com.samjay.AI_Powered.Job.Application.Tracker.repositories;

import com.samjay.AI_Powered.Job.Application.Tracker.entities.Reminder;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Repository
public interface ReminderRepository extends R2dbcRepository<Reminder, String> {

    Flux<Reminder> findAllByReminderDateAndIsSent(LocalDate reminderDate, boolean sent);

    @Modifying
    @Query("""
                UPDATE Reminders
                SET is_sent = true
                WHERE id = :reminderId
            """)
    Mono<Integer> updateIsSentById(String reminderId);

}