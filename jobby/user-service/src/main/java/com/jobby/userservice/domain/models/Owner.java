package com.jobby.userservice.domain.models;

import com.jobby.userservice.domain.vo.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Map;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Owner {
    private Long id;
    private User user;
    private Email alternativeEmail;
    private Map<String, String> secureParameters;
    private Instant createdAt;
    private Instant modifiedAt;
}
