package com.samjay.AI_Powered.Job.Application.Tracker.globalexception;

public class JobPageFetchException extends JobPostingException {

    public JobPageFetchException(Throwable cause) {

        super("Failed to fetch job page", cause);

    }
}