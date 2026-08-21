package com.jobby.infrastructure.security;

import com.jobby.ResultAssertions;
import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.ports.security.encrypt.EncryptionService;
import com.jobby.domain.ports.security.hashing.mac.MacService;
import com.jobby.infrastructure.security.fields.IndexedField;
import com.jobby.infrastructure.security.policies.FetchSecurityPolicy;
import com.jobby.infrastructure.security.policies.StorageSecurityPolicy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SecurityOrchestratorTest {

    @Mock
    private EncryptionService encryptionService;

    @Mock
    private MacService macService;

    @InjectMocks
    private SecurityOrchestrator securityOrchestrator;

    private static final String VALID_INPUT_STRING = "payload";
    private static final byte[] VALID_INPUT_BYTES = VALID_INPUT_STRING.getBytes(StandardCharsets.UTF_8);

    private static IndexedField STORAGE_INDEXED_FIELD;
    private static IndexedField FETCH_INDEXED_FIELD;

    @BeforeEach
    void setup(){
        STORAGE_INDEXED_FIELD = new IndexedField(VALID_INPUT_STRING, null, null);
        FETCH_INDEXED_FIELD = new IndexedField(null, VALID_INPUT_BYTES, VALID_INPUT_BYTES);
    }

    @Nested
    class SecureMethod{
        @Test
        public void whenSecureMethodIsCalled_ThenReturnsSecureContextClass(){
            var response = securityOrchestrator.secure();

            Assertions.assertNotNull(response);
            Assertions.assertInstanceOf(SecurityOrchestrator.SecureContext.class, response);
        }
    }

    @Nested
    class ReverseMethod{
        @Test
        public void whenReverseMethodIsCalled_ThenReturnsReverseContextClass(){
            var response = securityOrchestrator.reverse();

            Assertions.assertNotNull(response);
            Assertions.assertInstanceOf(SecurityOrchestrator.ReverseContext.class, response);
        }
    }

    @Nested
    class IndexMethod{
        @Test
        public void givenDataIsNull_WhenIndexMethodIsCalled_ThenReturnsNull(){
            var response = securityOrchestrator.index(null);

            verify(macService, never()).generateMac(anyString());
            ResultAssertions.assertSuccess(response, null);
        }

        @Test
        public void givenDataIsAnything_WhenIndexMethodIsCalled_ThenReturnsSuccess(){
            Result<byte[], Error> expectedResponse = Result.success("anything".getBytes(StandardCharsets.UTF_8));
            when(macService.generateMac("anything"))
                    .thenReturn(expectedResponse);

            var response = securityOrchestrator.index("anything");

            verify(macService, times(1)).generateMac(anyString());
            ResultAssertions.assertSuccess(response, expectedResponse.data());
        }
    }

    @Nested
    class SecureContextClass{
        @Test
        public void givenStorageSecurityPolicyIsNotSet_WhenSecureMethodIsCalled_EncryptsHashAndReturnsSuccess(){

            Result<byte[], Error> expectedResponse = Result.success("anything".getBytes(StandardCharsets.UTF_8));

            when(encryptionService.encryptAsBytes(anyString()))
                    .thenReturn(expectedResponse);
            when(macService.generateMac(anyString()))
                    .thenReturn(expectedResponse);

            var response = securityOrchestrator.secure()
                    .add(STORAGE_INDEXED_FIELD)
                    .build();

            verify(encryptionService, times(1))
                    .encryptAsBytes(anyString());

            verify(macService, times(1))
                    .generateMac(anyString());

            ResultAssertions.assertSuccess(response);
            Assertions.assertEquals(STORAGE_INDEXED_FIELD.getIndex(), expectedResponse.data());
            Assertions.assertEquals(STORAGE_INDEXED_FIELD.getData(), expectedResponse.data());
            Assertions.assertEquals("payload", STORAGE_INDEXED_FIELD.getPayload());
        }

        @Test
        public void givenPolicyIsOnlyEncryption_WhenSecureMethodIsCalled_EncryptsAndReturnsSuccess(){

            Result<byte[], Error> expectedResponse = Result.success("anything".getBytes(StandardCharsets.UTF_8));

            when(encryptionService.encryptAsBytes(anyString()))
                    .thenReturn(expectedResponse);

            var response = securityOrchestrator.secure()
                    .add(STORAGE_INDEXED_FIELD, StorageSecurityPolicy.SECURED_ONLY_ENCRYPTION)
                    .build();

            verify(encryptionService, times(1))
                    .encryptAsBytes(anyString());

            verify(macService, times(0))
                    .generateMac(anyString());

            ResultAssertions.assertSuccess(response);
            Assertions.assertNull(STORAGE_INDEXED_FIELD.getIndex());
            Assertions.assertEquals(STORAGE_INDEXED_FIELD.getData(), expectedResponse.data());
            Assertions.assertEquals("payload", STORAGE_INDEXED_FIELD.getPayload());
        }

        @Test
        public void givenPolicyIsOnlyHashing_WhenSecureMethodIsCalled_HashAndReturnsSuccess(){

            Result<byte[], Error> expectedResponse = Result.success("anything".getBytes(StandardCharsets.UTF_8));

            when(macService.generateMac(anyString()))
                    .thenReturn(expectedResponse);

            var response = securityOrchestrator.secure()
                    .add(STORAGE_INDEXED_FIELD, StorageSecurityPolicy.SECURED_ONLY_HASHING)
                    .build();

            verify(encryptionService, times(0))
                    .encryptAsBytes(anyString());

            verify(macService, times(1))
                    .generateMac(anyString());

            ResultAssertions.assertSuccess(response);
            Assertions.assertNull(STORAGE_INDEXED_FIELD.getData());
            Assertions.assertEquals(STORAGE_INDEXED_FIELD.getIndex(), expectedResponse.data());
            Assertions.assertEquals("payload", STORAGE_INDEXED_FIELD.getPayload());
        }

        @ParameterizedTest
        @MethodSource("storagePoliciesStream")
        public void givenIndexedFieldAreNull_WhenSecureMethodIsCalled_ReturnsSuccessButNotGenerateChanges(
                StorageSecurityPolicy policy
        ){
            var response = securityOrchestrator.secure()
                    .add(null, policy)
                    .build();

            verify(encryptionService, times(0))
                    .encryptAsBytes(anyString());

            verify(macService, times(0))
                    .generateMac(anyString());

            ResultAssertions.assertSuccess(response);
            Assertions.assertNull(STORAGE_INDEXED_FIELD.getData());
            Assertions.assertNull(STORAGE_INDEXED_FIELD.getIndex());
        }

        private static Stream<Arguments> storagePoliciesStream(){
            return Stream.of(
                    Arguments.of(StorageSecurityPolicy.SECURED),
                    Arguments.of(StorageSecurityPolicy.SECURED_ONLY_ENCRYPTION),
                    Arguments.of(StorageSecurityPolicy.SECURED_ONLY_HASHING)
            );
        }
    }

    @Nested
    class ReverseContextClass{
        @Test
        public void givenFetchSecurityPolicyIsNotSet_WheReverseMethodIsCalled_ReverseEncryptsHashAndReturnsSuccess(){
            Result<String, Error> expectedResponse =
                    Result.success(VALID_INPUT_STRING);

            when(encryptionService.decryptFromBytes(any()))
                    .thenReturn(expectedResponse);

            var response = securityOrchestrator.reverse()
                    .add(FETCH_INDEXED_FIELD)
                    .build();

            verify(encryptionService, times(1))
                    .decryptFromBytes(any());

            ResultAssertions.assertSuccess(response);
            Assertions.assertEquals(VALID_INPUT_BYTES, FETCH_INDEXED_FIELD.getIndex());
            Assertions.assertEquals(VALID_INPUT_BYTES, FETCH_INDEXED_FIELD.getData());
            Assertions.assertEquals(VALID_INPUT_STRING, FETCH_INDEXED_FIELD.getPayload());
        }

        @ParameterizedTest
        @MethodSource("storagePoliciesStream")
        public void givenIndexedFieldAreNull_WhenReverseMethodIsCalled_ReturnsSuccessButNotGenerateChanges(
                FetchSecurityPolicy policy
        ){
            var response = securityOrchestrator.reverse()
                    .add(null, policy)
                    .build();

            verify(encryptionService, times(0))
                    .encryptAsBytes(anyString());

            verify(macService, times(0))
                    .generateMac(anyString());

            ResultAssertions.assertSuccess(response);
            Assertions.assertNull(STORAGE_INDEXED_FIELD.getData());
            Assertions.assertNull(STORAGE_INDEXED_FIELD.getIndex());
        }

        private static Stream<Arguments> storagePoliciesStream(){
            return Stream.of(
                    Arguments.of(FetchSecurityPolicy.SEALED_CIPHER_REFERENCED),
                    Arguments.of(FetchSecurityPolicy.UNSEALED)
            );
        }
    }
}
