package com.samjay.AI_Powered.Job.Application.Tracker.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "jobPostings")
public class JobPosting extends BaseEntity implements Persistable<String> {

    @Id
    @Column(value = "id")
    private String id;

    @Column(value = "candidate_id")
    private String candidateId;

    @Column(value = "title")
    private String title;

    @Column(value = "company")
    private String company;

    @Column(value = "location")
    private String location;

    @Column(value = "job_url")
    private String jobUrl;

    @Column(value = "description")
    private String description;

    @Column(value = "deadline")
    private LocalDateTime deadline;

    @Override
    public boolean isNew() {

        return getDateCreated() == null;

    }
}