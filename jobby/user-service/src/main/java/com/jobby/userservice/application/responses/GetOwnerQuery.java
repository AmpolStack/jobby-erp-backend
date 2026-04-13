package com.jobby.userservice.application.responses;

import com.jobby.userservice.domain.models.User;
import com.jobby.userservice.domain.vo.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GetOwnerQuery {
    private GetUserQuery user;
    private String alternativeEmail;
    private Instant createdAt;
    private Instant modifiedAt;
}
