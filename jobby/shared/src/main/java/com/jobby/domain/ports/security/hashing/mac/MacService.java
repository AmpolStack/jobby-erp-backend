package com.jobby.domain.ports.security.hashing.mac;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;

public interface MacService {
    Result<byte[], Error> generateMac(String data);
    Result<Boolean, Error> verifyMac(String data, byte[] mac);
}
