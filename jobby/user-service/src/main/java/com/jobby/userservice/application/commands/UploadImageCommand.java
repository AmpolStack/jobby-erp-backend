package com.jobby.userservice.application.commands;


public record UploadImageCommand(long userId,
                                 String originalFilename,
                                 byte[] content,
                                 long size,
                                 String contentType) {
}
