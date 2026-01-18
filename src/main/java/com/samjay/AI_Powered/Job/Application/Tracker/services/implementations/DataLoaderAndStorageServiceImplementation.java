package com.samjay.AI_Powered.Job.Application.Tracker.services.implementations;

import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.samjay.AI_Powered.Job.Application.Tracker.services.DataLoaderAndStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.samjay.AI_Powered.Job.Application.Tracker.utils.Utility.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataLoaderAndStorageServiceImplementation implements DataLoaderAndStorageService {

    private final VectorStore vectorStore;

    private final BlobContainerAsyncClient blobContainerAsyncClient;


    @Override
    public Mono<Void> loadAndStoreResumeToVectorStore(String blobName, String candidateId, String resumeId) {

        return Mono.fromRunnable(() -> {

                    try {

                        BlobAsyncClient blobAsyncClient = blobContainerAsyncClient.getBlobAsyncClient(blobName);

                        Path tempFile = Files.createTempFile("cv-" + System.currentTimeMillis() + "-", ".pdf");

                        blobAsyncClient.downloadToFile(tempFile.toString(), true).block();

                        long fileSize = Files.size(tempFile);

                        log.info("Downloaded file size: {} bytes", fileSize);

                        Resource resource = new FileSystemResource(tempFile);

                        var reader = new PagePdfDocumentReader(resource, PdfDocumentReaderConfig.builder().build());

                        List<Document> documents = reader.read();

                        for (int i = 0; i < documents.size(); i++) {

                            Document document = documents.get(i);

                            document.getMetadata().put(CANDIDATE_ID, candidateId);

                            document.getMetadata().put(RESUME_ID, resumeId);

                            document.getMetadata().put(DOCUMENT_TYPE, "CV");

                            document.getMetadata().put(CHUNK_SIZE, i);
                        }

                        vectorStore.add(documents);

                        Files.deleteIfExists(tempFile);

                    } catch (IOException e) {

                        log.error("Error processing file for candidateId: {}, resumeId: {}", candidateId, resumeId, e);
                    }

                })
                .doOnError(e -> log.error("Error loading and storing resume to vector store", e))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}