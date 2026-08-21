package com.jobby.infrastructure.security.fields;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.Transient;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = "payload")
public class IndexedField{
    @JsonIgnore
    @Transient
    public transient String payload;

    @NotNull
    private byte[] data;

    @NotNull
    private byte[] index;
}
