package com.jobby.userservice.application.responses;


public record OwnerResponse(UserResponse user,
                            String alternativeEmail) {
}
