package com.samjay.AI_Powered.Job.Application.Tracker.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResumeResponseDto {

    private String id;

    private String resumeName;

}