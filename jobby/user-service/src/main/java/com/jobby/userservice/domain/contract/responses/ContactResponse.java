package com.jobby.userservice.domain.responses;


public record ContactResponse(String name,
                              String description,
                              boolean isPublic,
                              String value) {
}
