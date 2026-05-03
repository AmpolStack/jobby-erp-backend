package com.jobby.infrastructure.autoconfiguration;

import com.jobby.infrastructure.configurations.FileStorageConfig;
import com.jobby.infrastructure.configurations.FileStorageSetupConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;


@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({FileStorageSetupConfig.class})
public class FileStorageServiceAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties(prefix = "app.file-storage.setup")
    @Validated
    public FileStorageSetupConfig fileStorageSetupConfig(){
        return new FileStorageSetupConfig();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties(prefix = "app.file-storage.sources")
    @Validated
    public FileStorageConfig fileStorageConfig(){
        return new FileStorageConfig();
    }


    @ConditionalOnMissingBean
    @Bean
    public S3Client s3Client(FileStorageSetupConfig config) {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(config.getAccessKey(), config.getSecretKey());

        return S3Client.builder()
                .endpointOverride(URI.create(config.getEndpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.US_EAST_1)
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .forcePathStyle(true)
                .build();
    }
}
