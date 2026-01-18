package com.samjay.AI_Powered.Job.Application.Tracker.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Reminders")
public class Reminder extends BaseEntity implements Persistable<String> {

    @Id
    @Column(value = "id")
    private String id;

    @Column(value = "application_id")
    private String applicationId;

    @Column(value = "reminder_date")
    private LocalDate reminderDate;

    @Column(value = "is_sent")
    private boolean isSent;

    @Override
    public boolean isNew() {

        return getDateCreated() == null;

    }
}