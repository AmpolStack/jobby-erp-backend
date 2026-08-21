package com.jobby.infrastructure.adapter;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.mobility.validator.ValidationChain;
import com.jobby.domain.ports.FileStorageService;
import com.jobby.infrastructure.configurations.FileStorageConfig;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import java.net.URL;
import java.time.Duration;

@Slf4j
@AllArgsConstructor
public class FileStorageServiceAdapter implements FileStorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final FileStorageConfig config;
    private final ObservationRegistry observationRegistry;

    public Result<String, Error> upload(byte[] content,
                                        String mimeType,
                                        String key) {
        return ValidationChain.create()
                .validateNotNull(content, "uploaded file")
                .validateNotBlank(mimeType, "uploaded mimetype")
                .validateInternalNotBlank(key, "uploaded file key")
                .build()
                .flatMap(v -> {
                    var observation = Observation.createNotStarted("filestorage.upload", observationRegistry)
                            .lowCardinalityKeyValue("bucket", config.getBucket())
                            .start();

                    try{
                        this.s3Client.putObject(
                                PutObjectRequest.builder()
                                        .bucket(this.config.getBucket())
                                        .contentType(mimeType)
                                        .key(key)
                                        .build(),
                                RequestBody.fromBytes(content)
                        );
                        observation.stop();
                        var responseUrl = this.buildUrl(key);
                        return Result.success(responseUrl);
                    }
                    catch (NoSuchBucketException e){
                        observation.error(e);
                        log.error("[ITS_CONFIGURATION_ERROR] S3 bucket missing: bucket={}", this.config.getBucket(), e);
                        return Result.failure(ErrorType.ITS_CONFIGURATION_ERROR,
                                new Field("file storage config",
                                        e.getClass().getSimpleName() + ": bucket " + this.config.getBucket() + " does not exist"));
                    }
                    catch (S3Exception e){
                        observation.error(e);
                        var detail = e.awsErrorDetails();
                        var reason = detail != null ? detail.errorMessage() : e.getMessage();
                        log.error("[ITS_UNKNOWN_ERROR] S3 upload failed: key={}, status={}", key, e.statusCode(), e);
                        return Result.failure(ErrorType.ITS_UNKNOWN_ERROR,
                                new Field("file storage",
                                        e.getClass().getSimpleName() + ": " + reason));
                    }
                    catch (SdkClientException e){
                        observation.error(e);
                        log.error("[ITS_EXTERNAL_SERVICE_FAILURE] S3 connection failed: endpoint={}", this.config.getEndpoint(), e);
                        return Result.failure(ErrorType.ITS_EXTERNAL_SERVICE_FAILURE,
                                new Field("file storage",
                                        e.getClass().getSimpleName() + ": could not connect to file server"));
                    }
                });
    }

    public Result<URL, Error> getSigned(String key) {
        return ValidationChain.create()
                .validateInternalNotBlank(key, "deletion file key")
                .build()
                .flatMap(v -> {
                    var observation = Observation.createNotStarted("filestorage.get-signed", observationRegistry).start();

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
                        observation.stop();
                        return Result.success(presignedRequest.url());

                    } catch (S3Exception e) {
                        observation.error(e);
                        log.warn("[ITS_INVALID_STATE] S3 presigned URL failed: key={}", key, e);
                        return Result.failure(ErrorType.ITS_INVALID_STATE,
                                new Field("file",
                                        e.getClass().getSimpleName() + ": error generating presigned URL"));
                    }
                });
    }

    public Result<Void, Error> delete(String url){
        return ValidationChain.create()
                .validateNotBlank(url, "image url")
                .build()
                .flatMap(v -> {
                    var observation = Observation.createNotStarted("filestorage.delete", observationRegistry)
                            .lowCardinalityKeyValue("bucket", config.getBucket())
                            .start();

                    var key = this.extractKeyFromUrl(url);

                    try{
                        this.s3Client.deleteObject(
                                DeleteObjectRequest.builder()
                                .bucket(this.config.getBucket())
                                .key(key)
                                .build());

                        observation.stop();
                        return Result.success();
                    }
                    catch (NoSuchBucketException e){
                        observation.error(e);
                        log.error("[ITS_CONFIGURATION_ERROR] S3 bucket missing on delete: bucket={}", this.config.getBucket(), e);
                        return Result.failure(ErrorType.ITS_CONFIGURATION_ERROR,
                                new Field("file storage config",
                                        e.getClass().getSimpleName() + ": bucket " + this.config.getBucket() + " does not exist"));
                    }
                    catch (S3Exception e){
                        observation.error(e);
                        var detail = e.awsErrorDetails();
                        var reason = detail != null ? detail.errorMessage() : e.getMessage();
                        log.error("[ITS_UNKNOWN_ERROR] S3 delete failed: key={}", key, e);
                        return Result.failure(ErrorType.ITS_UNKNOWN_ERROR,
                                new Field("file storage",
                                        e.getClass().getSimpleName() + ": " + reason));
                    }
                    catch (SdkClientException e){
                        observation.error(e);
                        log.error("[ITS_EXTERNAL_SERVICE_FAILURE] S3 connection failed on delete: endpoint={}", this.config.getEndpoint(), e);
                        return Result.failure(ErrorType.ITS_EXTERNAL_SERVICE_FAILURE,
                                new Field("file storage",
                                        e.getClass().getSimpleName() + ": could not connect to file server"));

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
