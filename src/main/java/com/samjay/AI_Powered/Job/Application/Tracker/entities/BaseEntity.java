package com.samjay.AI_Powered.Job.Application.Tracker.entities;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class BaseEntity {

    @CreatedDate
    @Column(value = "date_created")
    private LocalDateTime dateCreated;

    @LastModifiedDate
    @Column(value = "date_updated")
    private LocalDateTime dateUpdated;

}