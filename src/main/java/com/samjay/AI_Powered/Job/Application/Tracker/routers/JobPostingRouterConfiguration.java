package com.samjay.AI_Powered.Job.Application.Tracker.routers;

import com.samjay.AI_Powered.Job.Application.Tracker.handlers.JobPostingHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@RequiredArgsConstructor
public class JobPostingRouterConfiguration {

    private final JobPostingHandler jobPostingHandler;

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/api/candidate/job-postings",
                            produces = {MediaType.APPLICATION_JSON_VALUE},
                            method = RequestMethod.GET,
                            beanClass = JobPostingHandler.class,
                            beanMethod = "fetchJobPostingsHandler",
                            operation = @Operation(
                                    operationId = "fetchJobPostingsHandler",
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "Successful operation",
                                                    content = @Content(
                                                            schema = @Schema(
                                                                    implementation = com.samjay.AI_Powered.Job.Application.Tracker.dtos.response.ApiResponse.class
                                                            )
                                                    )
                                            ),
                                            @ApiResponse(
                                                    responseCode = "400",
                                                    description = "Bad request",
                                                    content = @Content(
                                                            schema = @Schema(
                                                                    implementation = com.samjay.AI_Powered.Job.Application.Tracker.dtos.response.ApiResponse.class
                                                            )
                                                    )
                                            )
                                    }
                            )
                    )
            }
    )
    public RouterFunction<ServerResponse> candidateJobPostingRouterFunction() {

        return RouterFunctions
                .route()
                .GET("/api/candidate/job-postings", jobPostingHandler::fetchJobPostingsHandler)
                .build();
    }
}