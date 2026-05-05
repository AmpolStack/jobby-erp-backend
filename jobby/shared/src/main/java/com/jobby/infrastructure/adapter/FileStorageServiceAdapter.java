package com.jobby.infrastructure.adapter;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.mobility.validator.ValidationChain;
import com.jobby.infrastructure.configurations.FileStorageConfig;
import lombok.AllArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;

@AllArgsConstructor
public class FileStorageServiceAdapter {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final FileStorageConfig config;

    public Result<String, Error> upload(byte[] content,
                                        String mimeType,
                                        String key) {
        return ValidationChain.create()
                .validateNotNull(content, "uploaded file")
                .validateNotBlank(mimeType, "uploaded mimetype")
                .validateInternalNotBlank(key, "uploaded file key")
                .build()
                .flatMap(v -> {

                    try{
                        this.s3Client.putObject(
                                PutObjectRequest.builder()
                                        .bucket(this.config.getBucket())
                                        .contentType(mimeType)
                                        .key(key)
                                        .build(),
                                RequestBody.fromBytes(content)
                        );
                    }
                    catch (NoSuchBucketException e){
                        return Result.failure(ErrorType.ITS_CONFIGURATION_ERROR,
                                new Field("file storage config",
                                        "The bucket " + this.config.getBucket() + " is not exist " + e));
                    }
                    catch (S3Exception e){
                        return Result.failure(ErrorType.ITS_UNKNOWN_ERROR,
                                new Field("file storage",
                                        "Error in storage: " + e));
                    }
                    catch (SdkClientException e){
                        return Result.failure(ErrorType.ITS_EXTERNAL_SERVICE_FAILURE,
                                new Field("file storage",
                                        "It could not connect to the file server: " + e));
                    }

                    var responseUrl = this.buildUrl(key);
                    return Result.success(responseUrl);
                });
    }

    public Result<URL, Error> getSigned(String key) {
        return ValidationChain.create()
                .validateInternalNotBlank(key, "deletion file key")
                .build()
                .flatMap(v -> {
                    try {
                        var getObjectRequest = GetObjectRequest.builder()
                                .bucket(this.config.getBucket())
                                .key(key)
                                .build();

                        var  presignRequest = GetObjectPresignRequest.builder()
                                .getObjectRequest(getObjectRequest)
                                .signatureDuration(Duration.ofMinutes(15))
                                .build();

                        var presignedRequest = this.s3Presigner.presignGetObject(presignRequest);

                        return Result.success(presignedRequest.url());

                    } catch (S3Exception e) {
                        return Result.failure(ErrorType.ITS_INVALID_STATE,
                                new Field("file",
                                        "Error in file deletion: " + e));
                    }
                });
    }

    public Result<Void, Error> delete(String url){
        return ValidationChain.create()
                .validateNotBlank(url, "image url")
                .build()
                .flatMap(v -> {

                    var key = this.extractKeyFromUrl(url);

                    try{
                        this.s3Client.deleteObject(
                                DeleteObjectRequest.builder()
                                .bucket(this.config.getBucket())
                                .key(key)
                                .build());

                        return Result.success();
                    }
                    catch (NoSuchBucketException e){
                        return Result.failure(ErrorType.ITS_CONFIGURATION_ERROR,
                                new Field("file storage config",
                                        "The bucket " + this.config.getBucket() + " is not exist " + e));
                    }
                    catch (S3Exception e){
                        return Result.failure(ErrorType.ITS_UNKNOWN_ERROR,
                                new Field("file storage",
                                        "Error in storage: " + e));
                    }
                    catch (SdkClientException e){
                        return Result.failure(ErrorType.ITS_EXTERNAL_SERVICE_FAILURE,
                                new Field("file storage",
                                        "It could not connect to the file server: " + e));

                    }}
                );
    }

    public String buildUrl(String key) {
        return String.format("%s/%s/%s", this.config.getEndpoint(), this.config.getBucket(), key);
    }

    public String extractKeyFromUrl(String url) {
        // "http://localhost:9000/mi-app/abc123/users/u1/avatar.webp"
        //  → "abc123/users/u1/avatar.webp"
        String prefix = this.config.getEndpoint() + "/" + this.config.getBucket() + "/";
        return url.replace(prefix, "");
    }

}
