package com.jobby.userservice.application.queries;


public record GetContactQuery(String name,
                              String description,
                              boolean isPublic,
                              String value) {
}
