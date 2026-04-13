package com.jobby.userservice.domain.models;

import com.jobby.userservice.domain.vo.Email;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.util.Map;

@Setter
@Getter
public class Owner {
    private Long id;
    private Email alternativeEmail;
    private User user;
    // TODO: Convert to VO
    private Map<String, String> secureParameters;
    private Instant createdAt;
    private Instant modifiedAt;
}
