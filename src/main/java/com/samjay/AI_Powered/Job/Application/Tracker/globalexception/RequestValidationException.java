package com.samjay.AI_Powered.Job.Application.Tracker.globalexception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class RequestValidationException extends RuntimeException {

    public RequestValidationException(String message) {

        super(message);

    }
}