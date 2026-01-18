package com.samjay.AI_Powered.Job.Application.Tracker.services;

import com.samjay.AI_Powered.Job.Application.Tracker.dtos.response.ApiResponse;
import com.samjay.AI_Powered.Job.Application.Tracker.dtos.response.JobPostingResponseDto;
import com.samjay.AI_Powered.Job.Application.Tracker.entities.JobPosting;
import reactor.core.publisher.Mono;

import java.util.List;

public interface JobPostingService {

    Mono<JobPosting> fetchJobDetailsFromAiAsync(String jobUrl, String candidateId);

    Mono<ApiResponse<List<JobPostingResponseDto>>> getAllJobPostingsForCandidate();

}