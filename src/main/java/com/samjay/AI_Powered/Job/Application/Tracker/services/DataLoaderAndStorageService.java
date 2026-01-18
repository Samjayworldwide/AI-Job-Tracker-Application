package com.samjay.AI_Powered.Job.Application.Tracker.services;

import reactor.core.publisher.Mono;


public interface DataLoaderAndStorageService {

    Mono<Void> loadAndStoreResumeToVectorStore(String blobName, String candidateId, String resumeId);

}