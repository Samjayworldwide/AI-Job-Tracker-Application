# AI Job Tracker & Resume Advisor

An AI-powered job tracking application that helps job applicants analyze job postings against their resumes, receive personalized improvement suggestions, and get automated reminder emails for job deadlines.

This project demonstrates **modern backend architecture**, **reactive programming**, **AI-powered retrieval (RAG)**, and **event-driven systems** using Java and Spring Boot.

## Key Features

* **Secure Authentication**

  * Email-based signup and login
  * Email verification using one-time verification codes

* **Resume Management**

  * Upload resumes (PDF/DOC)
  * Store files securely in **Azure Blob Storage**
  * Track resume metadata in **MySQL**

* **AI-Powered Job Suggestions**

  * Extract job descriptions from URLs using **Jsoup**
  * Convert resumes into embeddings
  * Perform semantic search using **PGVector**
  * Generate tailored job application suggestions with **Spring AI + OpenAI**
  * Stream AI responses to the client in real time

* **Automated Job Reminders**

  * Save job application deadlines
  * Daily scheduled checks for upcoming reminders
  * Asynchronous email delivery using **RabbitMQ**

* **Scalable & Reactive**

  * Built with **Spring WebFlux**
  * Event-driven architecture using **Kafka** and **RabbitMQ**
  * Fully containerized with **Docker**

## Architecture Overview

```
Client
  │
  ▼
Spring WebFlux API
  │
  ├── Authentication & Email Verification
  │
  ├── Resume Upload Service
  │     ├── Azure Blob Storage
  │     ├── MySQL
  │     └── Kafka (Resume Processing Events)
  │
  ├── Resume Embedding Processor (Kafka Consumer)
  │     ├── Download Resume
  │     ├── Generate Embeddings
  │     └── Store in PGVector (PostgreSQL)
  │
  ├── Job Analysis Service
  │     ├── Redis (URL Caching)
  │     ├── WebClient + Jsoup
  │     └── Spring AI (OpenAI)
  │
  ├── Scheduler
  │     └── RabbitMQ (Email Reminders)
  │
  └── Email Service (RabbitMQ Consumer)
```

## Application Workflow

### 1️User Registration & Verification

* User signs up with an email address
* Verification code is sent via email
* Account is activated after successful verification

### 2️Resume Upload & Processing

* User uploads a resume
* Resume file is stored in **Azure Blob Storage**
* Resume metadata is saved in **MySQL**
* Resume blob name is published to a **Kafka topic**

**Kafka Consumer Flow:**

* Downloads resume from Azure Blob Storage
* Converts resume text into embeddings
* Saves embeddings into **PostgreSQL (PGVector)**

### 3️Job URL Analysis & AI Suggestions

* User selects a resume
* User provides a job posting URL
* URL content is cached in **Redis**
* Job description is extracted using **Jsoup**
* Resume embeddings are retrieved from PGVector
* AI generates tailored job application suggestions
* Suggestions are **streamed** to the client in real time

### 4️Job Tracking & Reminders

* Job application details and deadlines are saved
* **Spring Scheduler** runs daily
* Jobs with reminders due today are identified
* Reminder events are published to **RabbitMQ**
* Email service consumes messages and sends reminder emails

## Tech Stack

### Backend

* **Java**
* **Spring Boot**
* **Spring WebFlux**
* **Spring AI**

### AI & Search

* **OpenAI**
* **PGVector (PostgreSQL Vector Store)**

### Messaging & Streaming

* **Kafka** – Resume processing pipeline
* **RabbitMQ** – Email notification handling

### Databases & Caching

* **MySQL** – Core application data
* **PostgreSQL + PGVector** – Embeddings storage
* **Redis** – URL caching

### Storage & Communication

* **Azure Blob Storage**
* **Java Mail Sender**

### Other Tools

* **Jsoup** – HTML parsing
* **Spring Scheduler**
* **Docker**

---

## Getting Started

### Prerequisites

* Java 21+
* Docker & Docker Compose
* MySQL
* PostgreSQL with PGVector
* Kafka
* RabbitMQ
* Redis
* Azure Blob Storage account
* OpenAI API Key

## Highlights

* Reactive, non-blocking API using WebFlux
* Event-driven design with Kafka & RabbitMQ
* Real-world AI Retrieval-Augmented Generation (RAG)
* Scalable and production-ready architecture
* Clean separation of concerns

## Author

**Samuel Mbanisi**
Backend Engineer | Java | Spring Boot | AI Systems
