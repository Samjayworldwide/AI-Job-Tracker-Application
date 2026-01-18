package com.samjay.AI_Powered.Job.Application.Tracker.globalexception;

public abstract class JobPostingException extends RuntimeException {

    protected JobPostingException(String message, Throwable cause) {

        super(message, cause);

    }
}