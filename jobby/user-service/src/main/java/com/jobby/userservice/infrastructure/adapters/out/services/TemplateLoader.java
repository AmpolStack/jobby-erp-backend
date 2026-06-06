package com.jobby.userservice.infrastructure.adapters.out.services;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class TemplateLoader {
    public Result<String, Error> load(String templateName) {
        try {
            ClassPathResource resource = new ClassPathResource("templates/" + templateName);
            var content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return Result.success(content);
        } catch (IOException e) {
            return Result.failure(ErrorType.ITS_CONFIGURATION_ERROR, new Field("template resource:" + templateName,
                    e.toString()));
        }
    }
}
