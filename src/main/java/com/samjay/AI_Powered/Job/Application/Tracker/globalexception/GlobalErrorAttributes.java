package com.samjay.AI_Powered.Job.Application.Tracker.globalexception;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.Map;

@Component
@Slf4j
public class GlobalErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {

        Map<String, Object> map = super.getErrorAttributes(request, options);

        Throwable ex = getError(request);

        if (ex instanceof RequestValidationException validationEx) {

            map.put("status", HttpStatus.BAD_REQUEST.value());

            map.put("error", "Validation Failed");

            map.put("message", validationEx.getMessage());

        } else {

            log.error("AN UNEXPECTED GLOBAL ERROR OCCURRED: {}", ex.getMessage());

            map.put("status", HttpStatus.BAD_REQUEST.value());

            map.put("message", "Oops, an error occurred. Please try again later.");

        }

        return map;

    }
}