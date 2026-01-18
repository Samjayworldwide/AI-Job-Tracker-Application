package com.samjay.AI_Powered.Job.Application.Tracker.routers;

import com.samjay.AI_Powered.Job.Application.Tracker.dtos.request.EmailVerificationCodeRequestDto;
import com.samjay.AI_Powered.Job.Application.Tracker.handlers.EmailVerificationHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
public class EmailVerificationRouterConfiguration {

    private final EmailVerificationHandler emailVerificationHandler;

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/send-verification-code",
                            produces = {MediaType.APPLICATION_JSON_VALUE},
                            method = RequestMethod.POST,
                            beanClass = EmailVerificationHandler.class,
                            beanMethod = "sendVerificationCodeHandler",
                            operation = @Operation(
                                    operationId = "sendVerificationCodeHandler",
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
                                    parameters = @Parameter(in = ParameterIn.QUERY, name = "email", required = true)
                            )
                    )
            }
    )
    public RouterFunction<ServerResponse> sendVerificationCodeRouterFunction() {

        return RouterFunctions
                .route()
                .POST("/send-verification-code", emailVerificationHandler::sendVerificationCodeHandler)
                .build();
    }

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/verify-code",
                            produces = {MediaType.APPLICATION_JSON_VALUE},
                            method = RequestMethod.POST,
                            beanClass = EmailVerificationHandler.class,
                            beanMethod = "verifyCodeHandler",
                            operation = @Operation(
                                    operationId = "verifyCodeHandler",
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
                                            ),
                                    },
                                    requestBody = @RequestBody(
                                            content = @Content(
                                                    schema = @Schema(
                                                            implementation = EmailVerificationCodeRequestDto.class
                                                    )
                                            )
                                    )
                            )
                    )
            }
    )
    public RouterFunction<ServerResponse> verifyCodeRouterFunction() {

        return RouterFunctions
                .route()
                .POST("/verify-code", emailVerificationHandler::verifyCodeHandler)
                .build();
    }
}