package com.jobby.userservice.application.responses;


public record ContactResponse(String name,
                              String description,
                              boolean isPublic,
                              String value) {
}
