package com.samjay.AI_Powered.Job.Application.Tracker.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.net.URI;
import java.net.URISyntaxException;

public class JobUrlValidator implements ConstraintValidator<ValidJobUrl, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (value == null || value.isBlank()) {
            return false;
        }

        try {

            URI uri = new URI(value);

            if (!uri.isAbsolute()) return false;

            String scheme = uri.getScheme();

            if (!("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))) {

                return false;

            }

            if (uri.getHost() == null)
                return false;

            return !isLocalOrPrivateHost(uri.getHost());

        } catch (URISyntaxException e) {

            return false;

        }
    }

    private boolean isLocalOrPrivateHost(String host) {

        return host.equalsIgnoreCase("localhost")
                || host.startsWith("127.")
                || host.startsWith("0.")
                || host.endsWith(".local");
    }
}
