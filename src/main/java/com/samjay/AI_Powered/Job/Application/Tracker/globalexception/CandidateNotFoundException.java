package com.samjay.AI_Powered.Job.Application.Tracker.globalexception;

public class CandidateNotFoundException extends JobPostingException {

    public CandidateNotFoundException(String email) {

        super("Candidate not found for email: " + email, null);

    }
}