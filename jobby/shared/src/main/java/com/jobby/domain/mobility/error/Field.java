package com.jobby.domain.mobility.error;

import lombok.*;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Field {
    @Setter
    private String instance;
    private String reason;
}
