package com.samjay.AI_Powered.Job.Application.Tracker.configurations;

import com.azure.storage.blob.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureBlobStorageConfiguration {

    @Value("${azure.blob.connection-string}")
    private String connectionString;

    @Value("${azure.blob.container-name}")
    private String containerName;

    @Bean
    public BlobClientBuilder getClient() {

        BlobClientBuilder blobClientBuilder = new BlobClientBuilder();

        blobClientBuilder.connectionString(connectionString);

        blobClientBuilder.containerName(containerName);

        return blobClientBuilder;

    }

    @Bean
    public BlobContainerClient blobContainerClient() {

        BlobServiceClient serviceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();

        return serviceClient.getBlobContainerClient(containerName);

    }

    @Bean
    public BlobContainerAsyncClient blobContainerAsyncClient() {

        BlobServiceAsyncClient serviceAsyncClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildAsyncClient();

        return serviceAsyncClient.getBlobContainerAsyncClient(containerName);

    }
}