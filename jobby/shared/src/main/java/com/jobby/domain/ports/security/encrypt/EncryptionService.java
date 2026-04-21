package com.jobby.domain.ports.security.encrypt;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;

public interface EncryptionService {
    Result<String, Error> encryptAsBase64(String data);
    Result<byte[], Error> encryptAsBytes(String data);
    Result<String, Error> decryptFromBase64(String cipherText);
    Result<String, Error> decryptFromBytes(byte[] cipherText);
}
