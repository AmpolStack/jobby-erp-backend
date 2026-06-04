package com.jobby.infrastructure.configurations;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FileStorageSetupConfig {
    @NotNull
    @NotBlank
    private String accessKey;
    @NotNull
    @NotBlank
    private String secretKey;
    @NotNull
    @NotBlank
    private String endpoint;
}
