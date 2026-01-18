package com.samjay.AI_Powered.Job.Application.Tracker.globalexception;

import com.samjay.AI_Powered.Job.Application.Tracker.dtos.response.ApiResponse;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@Order(-2)
public class GlobalErrorHandler extends AbstractErrorWebExceptionHandler {

    public GlobalErrorHandler(ErrorAttributes errorAttributes,
                              WebProperties webProperties,
                              ApplicationContext applicationContext,
                              ServerCodecConfigurer configurer) {

        super(errorAttributes, webProperties.getResources(), applicationContext);

        this.setMessageWriters(configurer.getWriters());

        this.setMessageReaders(configurer.getReaders());

    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {

        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);

    }

    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {

        Map<String, Object> errorPropertiesMap = getErrorAttributes(request, ErrorAttributeOptions.defaults());

        int status = (int) errorPropertiesMap.getOrDefault("status", 400);

        String message = (String) errorPropertiesMap.getOrDefault("message", "Unexpected error occurred");

        ApiResponse<Object> apiResponse = new ApiResponse<>(false, message, errorPropertiesMap);

        return ServerResponse.status(HttpStatus.valueOf(status))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(apiResponse);
    }
}