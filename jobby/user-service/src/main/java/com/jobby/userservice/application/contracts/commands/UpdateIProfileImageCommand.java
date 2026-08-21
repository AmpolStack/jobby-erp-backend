package com.jobby.userservice.application.contracts.commands;

import com.jobby.domain.ports.in.Command;

public record UpdateIProfileImageCommand(long userId,
                                         String originalFilename,
                                         byte[] content,
                                         long size,
                                         String contentType) implements Command {
}
