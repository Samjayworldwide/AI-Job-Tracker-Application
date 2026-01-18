package com.samjay.AI_Powered.Job.Application.Tracker.services;

import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface ReminderService {

    Mono<Void> createReminder(String applicationId, LocalDate remindAt);

}