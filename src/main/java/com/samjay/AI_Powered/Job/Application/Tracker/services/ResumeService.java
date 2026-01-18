package com.samjay.AI_Powered.Job.Application.Tracker.services;

import com.samjay.AI_Powered.Job.Application.Tracker.dtos.response.ApiResponse;
import com.samjay.AI_Powered.Job.Application.Tracker.dtos.response.ResumeResponseDto;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ResumeService {

    Mono<ApiResponse<String>> uploadResume(String resumeName, FilePart resumeFile);

    Mono<ApiResponse<List<ResumeResponseDto>>> fetchAllCandidateResumes();

}