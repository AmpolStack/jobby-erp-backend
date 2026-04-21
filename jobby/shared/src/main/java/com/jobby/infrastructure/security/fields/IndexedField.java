package com.jobby.infrastructure.security.fields;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Transient;

@AllArgsConstructor
@Getter
@Setter
public class IndexedField{
    @JsonIgnore
    @Transient
    public transient String payload;

    @NotNull
    private byte[] data;

    @NotNull
    private byte[] index;
}
