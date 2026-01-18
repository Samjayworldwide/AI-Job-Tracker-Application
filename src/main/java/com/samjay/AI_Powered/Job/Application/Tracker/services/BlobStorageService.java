package com.samjay.AI_Powered.Job.Application.Tracker.services;

import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

public interface BlobStorageService {

    Mono<String> uploadFileToAzureBlobStorage(FilePart filePart, String prefixName);

}