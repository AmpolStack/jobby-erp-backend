package com.jobby.infrastructure.security;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.ports.security.encrypt.EncryptionService;
import com.jobby.domain.ports.security.hashing.mac.MacService;
import com.jobby.infrastructure.security.fields.IndexedField;
import com.jobby.infrastructure.security.fields.ProtectedField;
import com.jobby.infrastructure.security.policies.FetchSecurityPolicy;
import com.jobby.infrastructure.security.policies.StorageSecurityPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class SecurityOrchestrator {
    private final EncryptionService encryptionService;
    private final MacService macService;

    public SecureContext secure() {
        return new SecureContext();
    }

    public Result<byte[], Error> index(String data){
        if(data == null) return null;
        return this.macService.generateMac(data);
    }

    public ReverseContext reverse() {
        return new ReverseContext();
    }

    public final class SecureContext {

        private final List<Supplier<Result<Void, Error>>> operations = new ArrayList<>();

        public SecureContext add(IndexedField field) {
            return add(field, StorageSecurityPolicy.SECURED);
        }

        public SecureContext add(IndexedField field, StorageSecurityPolicy policy) {
            if(field != null)
                operations.add(() -> secureIndexedField(field, policy));
            return this;
        }

        public SecureContext add(ProtectedField field) {
            if(field != null)
                operations.add(() -> secureProtectedField(field));
            return this;
        }

        public Result<Void, Error> build() {
            return executeAll(operations);
        }
    }

    public final class ReverseContext {

        private final List<Supplier<Result<Void, Error>>> operations = new ArrayList<>();

        public ReverseContext add(IndexedField field) {
            return add(field, FetchSecurityPolicy.UNSEALED);
        }

        public ReverseContext add(IndexedField field, FetchSecurityPolicy policy) {
            if(field != null)
                operations.add(() -> reverseIndexedField(field, policy));
            return this;
        }

        public ReverseContext add(ProtectedField field) {
            if(field != null)
                operations.add(() -> reverseProtectedField(field));
            return this;
        }

        public Result<Void, Error> build() {
            return executeAll(operations);
        }
    }


    private Result<Void, Error> secureIndexedField(IndexedField field, StorageSecurityPolicy policy) {
        return switch (policy) {
            case SECURED_ONLY_ENCRYPTION -> encryptionService.encryptAsBytes(field.getPayload())
                    .flatMap(bytes -> {
                        field.setData(bytes);
                        return Result.success();
                    });
            case SECURED_ONLY_HASHING -> macService.generateMac(field.getPayload())
                    .flatMap(bytes -> {
                        field.setIndex(bytes);
                        return Result.success();
                    });
            case SECURED -> encryptionService.encryptAsBytes(field.getPayload())
                    .flatMap(bytes -> {
                        field.setData(bytes);
                        return macService.generateMac(field.getPayload());
                    })
                    .flatMap(bytes -> {
                        field.setIndex(bytes);
                        return Result.success();
                    });
        };
    }

    private Result<Void, Error> secureProtectedField(ProtectedField field) {
        return encryptionService.encryptAsBytes(field.getPayload())
                .flatMap(bytes -> {
                    field.setData(bytes);
                    return Result.success();
                });
    }

    private Result<Void, Error> reverseIndexedField(IndexedField field, FetchSecurityPolicy policy) {
        return switch (policy) {
            case UNSEALED, SEALED_CIPHER_REFERENCED -> encryptionService.decryptFromBytes(field.getData())
                    .flatMap(decrypted -> {
                        field.setPayload(decrypted);
                        return Result.success();
                    });
        };
    }

    private Result<Void, Error> reverseProtectedField(ProtectedField field) {
        return encryptionService.decryptFromBytes(field.getData())
                .flatMap(decrypted -> {
                    field.setPayload(decrypted);
                    return Result.success();
                });
    }

    private Result<Void, Error> executeAll(List<Supplier<Result<Void, Error>>> operations) {
        Result<Void, Error> result = Result.success();

        for (Supplier<Result<Void, Error>> operation : operations) {
            result = operation.get();
            if (result.isFailure()) break;
        }

        operations.clear();
        return result;
    }
}
