package com.jobby.userservice.infrastructure.adapters.out.services;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class TemplateLoader {

    private static final Logger log = LoggerFactory.getLogger(TemplateLoader.class);

    public Result<String, Error> load(String templateName) {
        try {
            ClassPathResource resource = new ClassPathResource("templates/" + templateName);
            var content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return Result.success(content);
        } catch (IOException e) {
            log.error("[ITS_CONFIGURATION_ERROR] Email template not found: template={}", templateName, e);
            return Result.failure(ErrorType.ITS_CONFIGURATION_ERROR,
                    new Field("template:" + templateName,
                            e.getClass().getSimpleName() + ": template not found"));
        }
    }
}
