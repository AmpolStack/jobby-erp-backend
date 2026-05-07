package com.jobby.userservice.application.queries;


public record GetOwnerQuery(GetUserQuery user,
                            String alternativeEmail) {
}
