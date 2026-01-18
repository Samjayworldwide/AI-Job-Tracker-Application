package com.samjay.AI_Powered.Job.Application.Tracker.globalexception;

public class AiExtractionException extends JobPostingException {

    public AiExtractionException(Throwable cause) {

        super("AI failed to extract job details", cause);

    }
}