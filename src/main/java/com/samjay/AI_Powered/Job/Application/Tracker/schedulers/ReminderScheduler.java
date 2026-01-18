package com.samjay.AI_Powered.Job.Application.Tracker.schedulers;

import com.samjay.AI_Powered.Job.Application.Tracker.dtos.request.EmailRequestDto;
import com.samjay.AI_Powered.Job.Application.Tracker.publishers.EmailPublisher;
import com.samjay.AI_Powered.Job.Application.Tracker.repositories.ApplicationRepository;
import com.samjay.AI_Powered.Job.Application.Tracker.repositories.CandidateRepository;
import com.samjay.AI_Powered.Job.Application.Tracker.repositories.JobPostingRepository;
import com.samjay.AI_Powered.Job.Application.Tracker.repositories.ReminderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

import static com.samjay.AI_Powered.Job.Application.Tracker.utils.Utility.getJobReminderMailBody;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReminderScheduler {

    private final ReminderRepository reminderRepository;

    private final CandidateRepository candidateRepository;

    private final ApplicationRepository applicationRepository;

    private final JobPostingRepository jobPostingRepository;

    private final EmailPublisher emailPublisher;

    @Scheduled(cron = "0 0 0 * * *", zone = "Africa/Lagos")
    public void sendReminder() {

        LocalDate now = LocalDate.now();

        reminderRepository.findAllByReminderDateAndIsSent(now, false)
                .flatMap(reminder -> applicationRepository.findById(reminder.getApplicationId())
                        .flatMap(application -> jobPostingRepository.findById(application.getJobPostingId())
                                .flatMap(jobPosting -> candidateRepository.findById(application.getCandidateId())
                                        .flatMap(candidate -> {

                                            String emailBody = getJobReminderMailBody(jobPosting.getTitle(), jobPosting.getJobUrl());

                                            EmailRequestDto emailRequestDto = new EmailRequestDto(candidate.getEmail(), emailBody, "Job Application Reminder");

                                            return emailPublisher.queueEmail(emailRequestDto)
                                                    .doOnSuccess(result -> log.info("Reminder email sent to {}", candidate.getEmail()))
                                                    .doOnError(error -> log.error("Failed to send reminder email to {}", candidate.getEmail(), error))
                                                    .then(reminderRepository.updateIsSentById(reminder.getId()));
                                        })
                                )
                        )
                ).subscribe();
    }
}