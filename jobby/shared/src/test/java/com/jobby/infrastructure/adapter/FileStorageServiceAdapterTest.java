package com.jobby.infrastructure.adapter;

import com.jobby.ResultAssertions;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.infrastructure.configurations.FileStorageConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import java.net.URI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("FileStorageServiceAdapter - Unit Tests")
@ExtendWith(MockitoExtension.class)
class FileStorageServiceAdapterTest {

    @Mock private S3Client s3Client;
    @Mock private S3Presigner s3Presigner;
    @Mock private FileStorageConfig config;

    @Captor private ArgumentCaptor<PutObjectRequest> putObjectCaptor;
    @Captor private ArgumentCaptor<DeleteObjectRequest> deleteObjectCaptor;

    private FileStorageServiceAdapter adapter;

    private static final String ENDPOINT = "http://localhost:9000";
    private static final String BUCKET = "my-bucket";

    @BeforeEach
    void setUp() {
        adapter = new FileStorageServiceAdapter(s3Client, s3Presigner, config, null);
    }

    @Nested
    @DisplayName("upload method")
    class UploadMethod {

        @Test
        @DisplayName("valid parameters uploads and returns URL")
        void givenValidParameters_whenUpload_returnsUrl() {
            when(config.getEndpoint()).thenReturn(ENDPOINT);
            when(config.getBucket()).thenReturn(BUCKET);

            var result = adapter.upload("data".getBytes(), "image/webp", "users/u1/avatar.webp");

            ResultAssertions.assertSuccess(result);
            assertThat(result.data()).isEqualTo("http://localhost:9000/my-bucket/users/u1/avatar.webp");
        }

        @Test
        @DisplayName("null content returns failure")
        void givenNullContent_whenUpload_returnsFailure() {
            var result = adapter.upload(null, "image/webp", "key");

            ResultAssertions.assertFailure(result, ErrorType.VALIDATION_ERROR);
        }

        @Test
        @DisplayName("blank mimeType returns failure")
        void givenBlankMimeType_whenUpload_returnsFailure() {
            var result = adapter.upload(new byte[]{1}, "   ", "key");

            ResultAssertions.assertFailure(result, ErrorType.VALIDATION_ERROR);
        }

        @Test
        @DisplayName("blank key returns failure")
        void givenBlankKey_whenUpload_returnsFailure() {
            var result = adapter.upload(new byte[]{1}, "image/png", "");

            ResultAssertions.assertFailure(result, ErrorType.ITN_VALIDATION_BLANK);
        }

        @Test
        @DisplayName("NoSuchBucketException returns ITS_CONFIGURATION_ERROR")
        void givenNoSuchBucketException_whenUpload_returnsConfigurationError() {
            when(config.getBucket()).thenReturn(BUCKET);
            doThrow(NoSuchBucketException.builder().message("no bucket").build())
                    .when(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));

            var result = adapter.upload(new byte[]{1}, "image/png", "key");

            ResultAssertions.assertFailure(result, ErrorType.ITS_CONFIGURATION_ERROR);
            assertThat(result.error().getFields())
                    .anySatisfy(f -> assertThat(f.getInstance()).isEqualTo("file storage config"));
        }

        @Test
        @DisplayName("S3Exception returns ITS_UNKNOWN_ERROR")
        void givenS3Exception_whenUpload_returnsUnknownError() {
            when(config.getBucket()).thenReturn(BUCKET);
            doThrow(S3Exception.builder().message("s3 error").build())
                    .when(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));

            var result = adapter.upload(new byte[]{1}, "image/png", "key");

            ResultAssertions.assertFailure(result, ErrorType.ITS_UNKNOWN_ERROR);
            assertThat(result.error().getFields())
                    .anySatisfy(f -> assertThat(f.getInstance()).isEqualTo("file storage"));
        }

        @Test
        @DisplayName("SdkClientException returns ITS_EXTERNAL_SERVICE_FAILURE")
        void givenSdkClientException_whenUpload_returnsExternalServiceFailure() {
            when(config.getBucket()).thenReturn(BUCKET);
            doThrow(SdkClientException.create("connection failed"))
                    .when(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));

            var result = adapter.upload(new byte[]{1}, "image/png", "key");

            ResultAssertions.assertFailure(result, ErrorType.ITS_EXTERNAL_SERVICE_FAILURE);
            assertThat(result.error().getFields())
                    .anySatisfy(f -> assertThat(f.getInstance()).isEqualTo("file storage"));
        }

        @Test
        @DisplayName("passes correct bucket, key and contentType to S3")
        void givenValidParameters_whenUpload_passesCorrectRequest() {
            when(config.getEndpoint()).thenReturn(ENDPOINT);
            when(config.getBucket()).thenReturn(BUCKET);

            adapter.upload("data".getBytes(), "image/jpeg", "avatar.jpg");

            verify(s3Client).putObject(putObjectCaptor.capture(), any(RequestBody.class));
            assertThat(putObjectCaptor.getValue().bucket()).isEqualTo(BUCKET);
            assertThat(putObjectCaptor.getValue().key()).isEqualTo("avatar.jpg");
            assertThat(putObjectCaptor.getValue().contentType()).isEqualTo("image/jpeg");
        }
    }

    @Nested
    @DisplayName("getSigned method")
    class GetSignedMethod {

        @Test
        @DisplayName("valid key returns presigned URL")
        void givenValidKey_whenGetSigned_returnsUrl() throws Exception {
            when(config.getBucket()).thenReturn(BUCKET);
            var presignedRequest = mock(PresignedGetObjectRequest.class);
            var expectedUrl = URI.create("http://localhost:9000/my-bucket/key?signature=abc").toURL();
            when(presignedRequest.url()).thenReturn(expectedUrl);
            when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
                    .thenReturn(presignedRequest);

            var result = adapter.getSigned("reports/doc.pdf");

            ResultAssertions.assertSuccess(result);
            assertThat(result.data()).isEqualTo(expectedUrl);
        }

        @Test
        @DisplayName("blank key returns failure")
        void givenBlankKey_whenGetSigned_returnsFailure() {
            var result = adapter.getSigned("");

            ResultAssertions.assertFailure(result, ErrorType.ITN_VALIDATION_BLANK);
        }

        @Test
        @DisplayName("S3Exception returns ITS_INVALID_STATE")
        void givenS3Exception_whenGetSigned_returnsInvalidState() {
            when(config.getBucket()).thenReturn(BUCKET);
            when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
                    .thenThrow(S3Exception.builder().message("presign failed").build());

            var result = adapter.getSigned("doc.pdf");

            ResultAssertions.assertFailure(result, ErrorType.ITS_INVALID_STATE);
            assertThat(result.error().getFields())
                    .anySatisfy(f -> assertThat(f.getInstance()).isEqualTo("file"));
        }
    }

    @Nested
    @DisplayName("delete method")
    class DeleteMethod {

        @Test
        @DisplayName("valid URL deletes and returns success")
        void givenValidUrl_whenDelete_returnsSuccess() {
            when(config.getEndpoint()).thenReturn(ENDPOINT);
            when(config.getBucket()).thenReturn(BUCKET);

            var result = adapter.delete(ENDPOINT + "/" + BUCKET + "/users/u1/old.webp");

            ResultAssertions.assertSuccess(result);
        }

        @Test
        @DisplayName("blank URL returns failure")
        void givenBlankUrl_whenDelete_returnsFailure() {
            var result = adapter.delete("");

            ResultAssertions.assertFailure(result, ErrorType.VALIDATION_ERROR);
        }

        @Test
        @DisplayName("extracts key from URL and passes to S3")
        void givenValidUrl_whenDelete_extractsKeyAndDeletes() {
            when(config.getEndpoint()).thenReturn(ENDPOINT);
            when(config.getBucket()).thenReturn(BUCKET);

            adapter.delete(ENDPOINT + "/" + BUCKET + "/users/u1/old.webp");

            verify(s3Client).deleteObject(deleteObjectCaptor.capture());
            assertThat(deleteObjectCaptor.getValue().bucket()).isEqualTo(BUCKET);
            assertThat(deleteObjectCaptor.getValue().key()).isEqualTo("users/u1/old.webp");
        }

        @Test
        @DisplayName("NoSuchBucketException returns ITS_CONFIGURATION_ERROR")
        void givenNoSuchBucketException_whenDelete_returnsConfigurationError() {
            when(config.getEndpoint()).thenReturn(ENDPOINT);
            when(config.getBucket()).thenReturn(BUCKET);
            doThrow(NoSuchBucketException.builder().message("no bucket").build())
                    .when(s3Client).deleteObject(any(DeleteObjectRequest.class));

            var result = adapter.delete(ENDPOINT + "/" + BUCKET + "/key");

            ResultAssertions.assertFailure(result, ErrorType.ITS_CONFIGURATION_ERROR);
        }

        @Test
        @DisplayName("S3Exception returns ITS_UNKNOWN_ERROR")
        void givenS3Exception_whenDelete_returnsUnknownError() {
            when(config.getEndpoint()).thenReturn(ENDPOINT);
            when(config.getBucket()).thenReturn(BUCKET);
            doThrow(S3Exception.builder().message("s3 error").build())
                    .when(s3Client).deleteObject(any(DeleteObjectRequest.class));

            var result = adapter.delete(ENDPOINT + "/" + BUCKET + "/key");

            ResultAssertions.assertFailure(result, ErrorType.ITS_UNKNOWN_ERROR);
        }

        @Test
        @DisplayName("SdkClientException returns ITS_EXTERNAL_SERVICE_FAILURE")
        void givenSdkClientException_whenDelete_returnsExternalServiceFailure() {
            when(config.getEndpoint()).thenReturn(ENDPOINT);
            when(config.getBucket()).thenReturn(BUCKET);
            doThrow(SdkClientException.create("connection error"))
                    .when(s3Client).deleteObject(any(DeleteObjectRequest.class));

            var result = adapter.delete(ENDPOINT + "/" + BUCKET + "/key");

            ResultAssertions.assertFailure(result, ErrorType.ITS_EXTERNAL_SERVICE_FAILURE);
        }
    }

    @Nested
    @DisplayName("buildUrl method")
    class BuildUrlMethod {

        @Test
        @DisplayName("returns formatted endpoint/bucket/key URL")
        void givenKey_whenBuildUrl_returnsFormattedUrl() {
            when(config.getEndpoint()).thenReturn(ENDPOINT);
            when(config.getBucket()).thenReturn(BUCKET);

            var url = adapter.buildUrl("path/to/file.txt");

            assertThat(url).isEqualTo("http://localhost:9000/my-bucket/path/to/file.txt");
        }
    }

    @Nested
    @DisplayName("extractKeyFromUrl method")
    class ExtractKeyFromUrlMethod {

        @Test
        @DisplayName("given build URL returns original key")
        void givenBuildUrl_whenExtractKeyFromUrl_returnsKey() {
            when(config.getEndpoint()).thenReturn(ENDPOINT);
            when(config.getBucket()).thenReturn(BUCKET);
            var url = adapter.buildUrl("users/u1/avatar.webp");

            var key = adapter.extractKeyFromUrl(url);

            assertThat(key).isEqualTo("users/u1/avatar.webp");
        }
    }
}
