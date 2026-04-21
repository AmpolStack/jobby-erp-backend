package com.jobby.infrastructure.adapter.hashing.mac;

import com.jobby.infrastructure.configurations.MacConfig;
import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.mobility.validator.ValidationChain;
import com.jobby.domain.ports.security.hashing.mac.MacBuilder;
import com.jobby.domain.ports.security.hashing.mac.MacService;
import com.jobby.infrastructure.adapter.CryptoUtils;
import lombok.AllArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@AllArgsConstructor
public class HmacSha256Service implements MacService {

    private final MacBuilder macBuilder;
    private final MacConfig config;


    @Override
    public Result<byte[], Error> generateMac(String data) {
        if(data == null || data.isBlank()){
            return Result.success(new byte[0]);
        }

        return MacCryptography.validateConfig(config)
                .flatMap(v -> CryptoUtils.validateAndParseKey(
                        config.getAlgorithm(),
                        config.getSecretKey(),
                        MacCryptography.VALID_KEY_LENGTHS_BITS))
                .flatMap(key -> ValidationChain.create()
                        .validateInternalNotBlank(data, "data")
                        .build()
                        .flatMap(v -> macBuilder
                                .setData(data.getBytes(StandardCharsets.UTF_8))
                                .setKey(key)
                                .setAlgorithm(config.getAlgorithm())
                                .build()
                        )
                );
    }

    @Override
    public Result<Boolean, Error> verifyMac(String data, byte[] hmac) {
        return generateMac(data)
                .map(generatedHmac -> Arrays.equals(generatedHmac, hmac));
    }
}
