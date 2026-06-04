package com.jobby.userservice.domain.contract.commands;


public record UpdateIProfileImageCommand(long userId,
                                         String originalFilename,
                                         byte[] content,
                                         long size,
                                         String contentType) {
}
