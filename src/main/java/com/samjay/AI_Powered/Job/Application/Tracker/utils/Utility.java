package com.samjay.AI_Powered.Job.Application.Tracker.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.samjay.AI_Powered.Job.Application.Tracker.globalexception.ApplicationException;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
public class Utility {

    private Utility() {
    }

    public static final String EMAIL_QUEUE = "email-queue";

    public static final String VECTOR_STORE_TOPIC = "vector-store-topic";

    public static final String CANDIDATE_ID = "candidateId";

    public static final String RESUME_ID = "resumeId";

    public static final String DOCUMENT_TYPE = "DocumentType";

    public static final String CHUNK_SIZE = "ChunkSize";

    public static final String CACHE_KEY_CANDIDATE_RESUMES = "candidateResumes::";

    public static final String CACHE_KEY_JOB_URL = "jobUrl::";

    public static final String JOB_POSTING_CACHE_KEY = "jobPosting::";

    public static final String APPLICATION_CACHE_KEY = "application::";

    public static final Duration CACHE_TTL = Duration.ofMinutes(30);

    public static String generateVerificationCode() {

        Random random = new Random();

        int randomVerificationCodeNumber = random.nextInt(999999);

        String verificationCode = Integer.toString(randomVerificationCodeNumber);

        while (verificationCode.length() < 6) {
            verificationCode = "0".concat(verificationCode);
        }

        return verificationCode;
    }

    public static LocalDateTime getCurrentLocalDateTime() {

        return LocalDateTime.now();

    }

    public static String getVerificationCodeMailBody(String verificationCode) {

        return "<html>" +
                "<body>" +
                "<h2>Verify Your Email Address</h2>" +
                "<p>Thank you for registering with our AI Job tracker system.</p>" +
                "<p>To complete your registration, please use the verification code below:</p>" +
                "<h1>" + verificationCode + "</h1>" +
                "<p>Enter this code on the verification page to activate your account.</p>" +
                "<p>This verification code will expire in 10 minutes.</p>" +
                "<p>If you did not request this verification code, please ignore this email or contact support.</p>" +
                "</body>" +
                "</html>";
    }

    public static String getJobReminderMailBody(String jobTitle, String jobUrl) {

        return "<html>" +
                "<body>" +
                "<h2>Reminder: Apply for " + jobTitle + "</h2>" +
                "<p>Hi there,</p>" +
                "<p>This is a friendly reminder about the job opportunity you showed interest in.</p>" +
                "<p>Don't miss out on this great opportunity! The position is still available:</p>" +
                "<div style='background-color: #f9f9f9; padding: 15px; border-radius: 4px; margin: 20px 0;'>" +
                "<p style='margin: 5px 0;'><strong>Job Title:</strong> " + jobTitle + "</p>" +
                "</div>" +
                "<p>Click the button below to apply now:</p>" +
                "<p style='margin-top: 20px;'>" +
                "<a href='" + jobUrl + "' style='background-color: #4CAF50; color: white; padding: 12px 30px; text-decoration: none; border-radius: 4px; display: inline-block;'>Apply Now</a>" +
                "</p>" +
                "<p style='margin-top: 30px;'>Don't wait too long - great opportunities don't last forever!</p>" +
                "<p>Good luck with your application!</p>" +
                "<p>Best regards,<br>AI Job Tracker Team</p>" +
                "</body>" +
                "</html>";
    }

    public static String convertObjectToString(Object object) {

        ObjectMapper objectMapper = new ObjectMapper();

        try {

            return objectMapper.writeValueAsString(object);

        } catch (Exception e) {

            log.error("Error converting object to string: ", e);

            throw new ApplicationException("Error converting object to string");

        }
    }
}