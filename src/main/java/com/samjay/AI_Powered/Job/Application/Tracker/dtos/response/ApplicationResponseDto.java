package com.samjay.AI_Powered.Job.Application.Tracker.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationResponseDto {

    private String jobUrl;

    public String aiSuggestions;

}