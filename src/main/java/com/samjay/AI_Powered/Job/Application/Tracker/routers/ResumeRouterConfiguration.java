package com.samjay.AI_Powered.Job.Application.Tracker.routers;

import com.samjay.AI_Powered.Job.Application.Tracker.handlers.ResumeHandler;
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
public class ResumeRouterConfiguration {

    private final ResumeHandler resumeHandler;

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/api/resumes/upload",
                            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
                            produces = {MediaType.APPLICATION_JSON_VALUE},
                            method = RequestMethod.POST,
                            beanClass = ResumeHandler.class,
                            beanMethod = "uploadResumeHandler",
                            operation = @Operation(
                                    operationId = "uploadResumeHandler",
                                    responses = {
                                            @ApiResponse(responseCode = "200", description = "Successful operation"),
                                            @ApiResponse(responseCode = "400", description = "Bad request")
                                    }
                            )
                    )
            }
    )
    public RouterFunction<ServerResponse> uploadResumeRouterFunction() {

        return RouterFunctions
                .route()
                .POST("/api/resumes/upload", resumeHandler::uploadResumeHandler)
                .build();
    }

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/api/candidate/resumes",
                            produces = {MediaType.APPLICATION_JSON_VALUE},
                            method = RequestMethod.GET,
                            beanClass = ResumeHandler.class,
                            beanMethod = "getCandidateResumeHandler",
                            operation = @Operation(
                                    operationId = "getCandidateResumeHandler",
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
    public RouterFunction<ServerResponse> getCandidateResumeRouterFunction() {

        return RouterFunctions
                .route()
                .GET("/api/candidate/resumes", resumeHandler::getCandidateResumeHandler)
                .build();
    }
}