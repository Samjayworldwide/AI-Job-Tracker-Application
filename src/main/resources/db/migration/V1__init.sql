CREATE TABLE IF NOT EXISTS Candidates
(
    id CHAR(36) PRIMARY KEY,
    firstname VARCHAR(255) NOT NULL,
    lastname VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_candidates_email
    ON Candidates (email);

CREATE TABLE IF NOT EXISTS EmailVerifications
(
    id CHAR(36) PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    verification_code VARCHAR(255) NOT NULL,
    is_verified BIT DEFAULT false,
    date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_email_verifications_email
    ON EmailVerifications (email);

CREATE TABLE IF NOT EXISTS jobPostings
(
    id CHAR(36) PRIMARY KEY,
    candidate_id CHAR(36) NOT NULL,
    title VARCHAR(255) NOT NULL,
    company VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL,
    job_url VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    deadline TIMESTAMP,
    date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_jobposting_candidate
    FOREIGN KEY (candidate_id)
    REFERENCES Candidates(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS Resumes
(
    id CHAR(36) PRIMARY KEY,
    candidate_id CHAR(36) NOT NULL,
    resume_name VARCHAR(255) NOT NULL,
    blob_name VARCHAR(255) NOT NULL,
    upload_date TIMESTAMP NOT NULL,
    is_current BIT NOT NULL,
    date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_resume_candidate
    FOREIGN KEY (candidate_id)
    REFERENCES Candidates(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS Applications
(
    id CHAR(36) PRIMARY KEY,
    candidate_id CHAR(36) NOT NULL,
    job_posting_id CHAR(36) NOT NULL,
    resume_id CHAR(36) NOT NULL,
    ai_suggestions TEXT NOT NULL,
    date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_application_candidate
    FOREIGN KEY (candidate_id)
    REFERENCES Candidates(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
    CONSTRAINT fk_application_jobposting
    FOREIGN KEY (job_posting_id)
    REFERENCES jobPostings(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
    CONSTRAINT fk_application_resume
    FOREIGN KEY (resume_id)
    REFERENCES Resumes(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS Reminders
(
    id CHAR(36) PRIMARY KEY,
    application_id CHAR(36) NOT NULL,
    reminder_date TIMESTAMP NOT NULL,
    is_sent BIT NOT NULL,
    date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reminder_application
    FOREIGN KEY (application_id)
    REFERENCES Applications(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE
);