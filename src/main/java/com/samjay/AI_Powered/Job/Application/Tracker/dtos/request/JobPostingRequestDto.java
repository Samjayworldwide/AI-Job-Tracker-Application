package com.samjay.AI_Powered.Job.Application.Tracker.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobPostingRequestDto {

    private String jobTitle;

    private String companyName;

    private String jobLocation;

    private String jobDescription;

    private LocalDateTime applicationDeadline;

}