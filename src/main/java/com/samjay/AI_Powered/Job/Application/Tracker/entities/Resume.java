package com.samjay.AI_Powered.Job.Application.Tracker.entities;

import com.samjay.AI_Powered.Job.Application.Tracker.constants.ResumeProcessingStatus;
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
@Table(name = "Resumes")
public class Resume extends BaseEntity implements Persistable<String> {

    @Id
    @Column(value = "id")
    private String id;

    @Column(value = "candidate_id")
    private String candidateId;

    @Column(value = "resume_name")
    private String resumeName;

    @Column(value = "blob_name")
    private String blobName;

    @Column(value = "upload_date")
    private LocalDateTime uploadedDate;

    @Column(value = "is_current")
    private boolean isCurrent = true;

    @Column("processing_status")
    private ResumeProcessingStatus processingStatus;

    @Override
    public boolean isNew() {

        return getDateCreated() == null;

    }
}