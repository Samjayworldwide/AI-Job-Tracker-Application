package com.samjay.AI_Powered.Job.Application.Tracker.routers;

import com.samjay.AI_Powered.Job.Application.Tracker.dtos.request.CandidateRegistrationRequestDto;
import com.samjay.AI_Powered.Job.Application.Tracker.dtos.request.LoginRequestDto;
import com.samjay.AI_Powered.Job.Application.Tracker.handlers.CandidateHandler;
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
public class CandidateRouterConfiguration {

    private final CandidateHandler candidateHandler;

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/api/candidates/signup",
                            produces = {MediaType.APPLICATION_JSON_VALUE},
                            method = RequestMethod.POST,
                            beanClass = CandidateHandler.class,
                            beanMethod = "signUpCandidateHandler",
                            operation = @Operation(
                                    operationId = "signUpCandidateHandler",
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
                                    },
                                    requestBody = @RequestBody(
                                            content = @Content(
                                                    schema = @Schema(
                                                            implementation = CandidateRegistrationRequestDto.class
                                                    )
                                            )
                                    )
                            )
                    )
            }
    )
    public RouterFunction<ServerResponse> candidateSignUpRouteFunction() {

        return RouterFunctions
                .route()
                .POST("/api/candidates/signup", candidateHandler::signUpCandidateHandler)
                .build();
    }

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/api/candidates/login",
                            produces = {MediaType.APPLICATION_JSON_VALUE},
                            method = RequestMethod.POST,
                            beanClass = CandidateHandler.class,
                            beanMethod = "loginCandidateHandler",
                            operation = @Operation(
                                    operationId = "loginCandidateHandler",
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
                                    },
                                    requestBody = @RequestBody(
                                            content = @Content(
                                                    schema = @Schema(
                                                            implementation = LoginRequestDto.class
                                                    )
                                            )
                                    )
                            )
                    )
            }
    )
    public RouterFunction<ServerResponse> candidateLoginRouteFunction() {

        return RouterFunctions
                .route()
                .POST("/api/candidates/login", candidateHandler::loginCandidateHandler)
                .build();
    }
}