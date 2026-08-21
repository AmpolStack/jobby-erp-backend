package com.jobby.domain.ports;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import java.net.URL;

public interface FileStorageService {
    Result<String, Error> upload(byte[] content,
                                        String mimeType,
                                        String key);

    Result<URL, Error> getSigned(String key);
    Result<Void, Error> delete(String url);
    String buildUrl(String key);
    String extractKeyFromUrl(String url);
}
