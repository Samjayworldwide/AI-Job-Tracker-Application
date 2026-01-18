package com.samjay.AI_Powered.Job.Application.Tracker.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class JobPostingResponseDto {

    private String title;

    private String company;

    private String location;

    private String jobUrl;

    private String description;

    private LocalDateTime deadline;

}