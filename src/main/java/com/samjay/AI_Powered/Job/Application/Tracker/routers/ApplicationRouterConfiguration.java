package com.samjay.AI_Powered.Job.Application.Tracker.routers;

import com.samjay.AI_Powered.Job.Application.Tracker.dtos.request.JobFitEvaluationRequestDto;
import com.samjay.AI_Powered.Job.Application.Tracker.handlers.ApplicationHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
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
public class ApplicationRouterConfiguration {

    private final ApplicationHandler applicationHandler;

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/api/ai-recommendation",
                            produces = {MediaType.TEXT_EVENT_STREAM_VALUE},
                            method = RequestMethod.POST,
                            beanClass = ApplicationHandler.class,
                            beanMethod = "streamAiJobRecommendationHandler",
                            operation = @Operation(
                                    operationId = "streamAiJobRecommendationHandler",
                                    responses = {
                                            @ApiResponse(responseCode = "200", description = "Successful operation"),
                                            @ApiResponse(responseCode = "400", description = "Bad request")
                                    },
                                    requestBody = @RequestBody(
                                            content = @Content(
                                                    schema = @Schema(
                                                            implementation = JobFitEvaluationRequestDto.class
                                                    )
                                            )
                                    )
                            )
                    )
            }
    )
    public RouterFunction<ServerResponse> applicationRouteFunction() {

        return RouterFunctions
                .route()
                .POST("/api/ai-recommendation", applicationHandler::streamAiJobRecommendationHandler)
                .build();
    }

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/api/applications-with-ai-suggestions",
                            produces = {MediaType.APPLICATION_JSON_VALUE},
                            method = RequestMethod.GET,
                            beanClass = ApplicationHandler.class,
                            beanMethod = "getAllApplicationsWithAiSuggestionsHandler",
                            operation = @Operation(
                                    operationId = "getAllApplicationsWithAiSuggestionsHandler",
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
    public RouterFunction<ServerResponse> getAllApplicationsWithAiSuggestionsRouteFunction() {

        return RouterFunctions
                .route()
                .GET("/api/applications-with-ai-suggestions", applicationHandler::getAllApplicationsWithAiSuggestionsHandler)
                .build();
    }
}