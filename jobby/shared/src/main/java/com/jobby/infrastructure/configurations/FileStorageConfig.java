package com.jobby.infrastructure.configurations;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FileStorageConfig {
    @NotNull
    @NotBlank
    private String endpoint;
    @NotNull
    @NotBlank
    private String bucket;
}
