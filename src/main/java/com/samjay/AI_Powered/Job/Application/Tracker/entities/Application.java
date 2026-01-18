package com.samjay.AI_Powered.Job.Application.Tracker.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "applications")
public class Application extends BaseEntity implements Persistable<String> {

    @Id
    @Column(value = "id")
    private String id;

    @Column(value = "candidate_id")
    private String candidateId;

    @Column(value = "job_posting_id")
    private String jobPostingId;

    @Column(value = "resume_id")
    private String resumeId;

    @Column(value = "ai_suggestions")
    public String aiSuggestions;

    @Override
    public boolean isNew() {

        return getDateCreated() == null;

    }
}