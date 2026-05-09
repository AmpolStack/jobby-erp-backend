package com.jobby.userservice.domain.responses;


public record OwnerResponse(UserResponse user,
                            String alternativeEmail) {
}
