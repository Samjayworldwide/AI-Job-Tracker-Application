package com.samjay.AI_Powered.Job.Application.Tracker.handlers;

import com.samjay.AI_Powered.Job.Application.Tracker.dtos.response.ApiResponse;
import com.samjay.AI_Powered.Job.Application.Tracker.dtos.response.ResumeResponseDto;
import com.samjay.AI_Powered.Job.Application.Tracker.services.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ResumeHandler {

    private final ResumeService resumeService;

    public Mono<ServerResponse> uploadResumeHandler(ServerRequest request) {

        Mono<String> resumeNameMono = request.multipartData()
                .mapNotNull(data -> data.getFirst("resumeName"))
                .cast(FormFieldPart.class)
                .map(FormFieldPart::value);

        Mono<FilePart> resumeFileMono = request.multipartData()
                .mapNotNull(data -> data.getFirst("resumeFile"))
                .cast(FilePart.class);

        return Mono.zip(resumeNameMono, resumeFileMono)
                .flatMap(tuple -> {

                    String resumeName = tuple.getT1();

                    FilePart resumeFile = tuple.getT2();

                    if (resumeName == null || resumeName.isBlank())
                        return badRequest("Resume name is required");

                    if (resumeFile == null)
                        return badRequest("Resume file is required");

                    return resumeService.uploadResume(resumeName, resumeFile)
                            .flatMap(response -> response.isSuccessful()
                                    ? ServerResponse.ok().bodyValue(response)
                                    : ServerResponse.badRequest().bodyValue(response));
                })
                .onErrorResume(ClassCastException.class, e -> badRequest("Invalid form data format"))
                .onErrorResume(e -> badRequest("Error processing upload: " + e.getMessage()));
    }

    public Mono<ServerResponse> getCandidateResumeHandler(ServerRequest serverRequest) {

        Mono<ApiResponse<List<ResumeResponseDto>>> apiResponseMono = resumeService.fetchAllCandidateResumes();

        return apiResponseMono.flatMap(response -> {

            if (!response.isSuccessful())
                return ServerResponse.badRequest().body(Mono.just(response), ApiResponse.class);

            return ServerResponse.ok().body(Mono.just(response), ApiResponse.class);

        });
    }

    private Mono<ServerResponse> badRequest(String message) {

        ApiResponse<String> errorResponse = ApiResponse.<String>builder()
                .isSuccessful(false)
                .responseMessage(message)
                .build();

        return ServerResponse.badRequest().bodyValue(errorResponse);

    }
}