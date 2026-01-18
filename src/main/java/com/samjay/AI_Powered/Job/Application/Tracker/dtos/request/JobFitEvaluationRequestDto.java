package com.samjay.AI_Powered.Job.Application.Tracker.dtos.request;

import com.samjay.AI_Powered.Job.Application.Tracker.validator.ValidJobUrl;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class JobFitEvaluationRequestDto {

    @NotBlank(message = "Job URL is required")
    @ValidJobUrl
    private String jobUrl;

    @NotBlank(message = "Resume ID is required")
    private String resumeId;

}