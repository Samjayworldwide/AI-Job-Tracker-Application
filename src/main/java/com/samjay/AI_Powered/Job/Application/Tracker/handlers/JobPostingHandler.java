package com.samjay.AI_Powered.Job.Application.Tracker.handlers;

import com.samjay.AI_Powered.Job.Application.Tracker.dtos.response.ApiResponse;
import com.samjay.AI_Powered.Job.Application.Tracker.dtos.response.JobPostingResponseDto;
import com.samjay.AI_Powered.Job.Application.Tracker.services.JobPostingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobPostingHandler {

    private final JobPostingService jobPostingService;

    public Mono<ServerResponse> fetchJobPostingsHandler(ServerRequest serverRequest) {

        Mono<ApiResponse<List<JobPostingResponseDto>>> apiResponseMono = jobPostingService.getAllJobPostingsForCandidate();

        return apiResponseMono.flatMap(response -> {

            if (!response.isSuccessful())
                return ServerResponse.badRequest().body(Mono.just(response), ApiResponse.class);

            return ServerResponse.ok().body(Mono.just(response), ApiResponse.class);

        });
    }
}