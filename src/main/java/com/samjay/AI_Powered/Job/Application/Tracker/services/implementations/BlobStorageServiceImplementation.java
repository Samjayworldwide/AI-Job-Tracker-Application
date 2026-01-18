package com.samjay.AI_Powered.Job.Application.Tracker.services.implementations;

import com.azure.storage.blob.*;
import com.samjay.AI_Powered.Job.Application.Tracker.services.BlobStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlobStorageServiceImplementation implements BlobStorageService {

    private final BlobContainerAsyncClient blobContainerAsyncClient;

    @Override
    public Mono<String> uploadFileToAzureBlobStorage(FilePart filePart, String prefixName) {

        String blobName = prefixName + UUID.randomUUID() + "-" + filePart.filename();

        BlobAsyncClient blobAsyncClient = blobContainerAsyncClient.getBlobAsyncClient(blobName);

        Flux<ByteBuffer> byteBufferFlux = DataBufferUtils.join(filePart.content())
                .map(dataBuffer -> {

                    byte[] bytes = new byte[dataBuffer.readableByteCount()];

                    dataBuffer.read(bytes);

                    DataBufferUtils.release(dataBuffer);

                    return ByteBuffer.wrap(bytes);
                })
                .flux();

        return blobAsyncClient.upload(byteBufferFlux, null, true)
                .doOnSuccess(response -> log.info("Successfully uploaded blob: {}", blobName))
                .doOnError(e -> log.error("Error uploading file to Azure Blob Storage : {}", e.getMessage()))
                .thenReturn(blobName);
    }
}