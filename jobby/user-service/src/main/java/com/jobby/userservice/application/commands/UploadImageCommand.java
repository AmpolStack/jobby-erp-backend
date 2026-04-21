package com.jobby.userservice.application.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.InputStream;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UploadImageCommand {
    private int userId;
    private String originalFilename;
    private InputStream content;
    private long size;
    private String contentType;
}
