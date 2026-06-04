package com.jobby.userservice.domain.contract.responses;


public record ContactResponse(String name,
                              String description,
                              boolean isPublic,
                              String value) {
}
