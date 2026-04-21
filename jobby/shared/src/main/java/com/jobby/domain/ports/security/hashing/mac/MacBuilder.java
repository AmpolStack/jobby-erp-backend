package com.jobby.domain.ports.security.hashing.mac;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;

import java.security.Key;

public interface MacBuilder {
    MacBuilder setData(byte[] data);
    MacBuilder setKey(Key key);
    MacBuilder setAlgorithm(String algorithm);
    Result<byte[], Error> build();
}
