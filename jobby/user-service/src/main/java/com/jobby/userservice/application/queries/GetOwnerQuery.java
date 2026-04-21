package com.jobby.userservice.application.queries;

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
}
