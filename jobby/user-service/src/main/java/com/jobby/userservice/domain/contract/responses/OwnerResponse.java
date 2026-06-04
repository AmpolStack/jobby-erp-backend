package com.jobby.userservice.domain.contract.responses;


public record OwnerResponse(UserResponse user,
                            String alternativeEmail) {
}
