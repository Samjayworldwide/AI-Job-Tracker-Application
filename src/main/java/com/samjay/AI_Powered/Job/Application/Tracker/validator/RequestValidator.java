package com.samjay.AI_Powered.Job.Application.Tracker.validator;

import com.samjay.AI_Powered.Job.Application.Tracker.globalexception.RequestValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RequestValidator {

    private final Validator validator;

    public <T> T validate(T object) {

        var errors = validator.validate(object);

        if (errors.isEmpty()) {

            return object;

        } else {

            String errorDetails = errors
                    .stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));

            throw new RequestValidationException(errorDetails);

        }
    }
}