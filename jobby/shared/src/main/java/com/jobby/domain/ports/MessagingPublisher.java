package com.jobby.domain.ports;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import java.util.Map;

public interface MessagingPublisher {
   <T> Result<Void, Error> publish(String topicIdentifier, T data, int timeout);
   <T> void publishAsync(String topicIdentifier, T data);
   <T> Result<Void, Error> publish(String topicIdentifier, String key, T data, int timeout);
   <T> void publishAsync(String topicIdentifier, String key, T data);
   <T> Result<Void, Error> publish(String topicIdentifier, String key, T data, Map<String, String> metadata, int timeout);
   <T> void publishAsync(String topicIdentifier, String key, T data, Map<String, String> metadata);
}
