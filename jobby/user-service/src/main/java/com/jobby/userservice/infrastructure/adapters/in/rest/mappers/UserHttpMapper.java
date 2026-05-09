package com.jobby.userservice.infrastructure.adapters.in.mappers;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.error.ErrorType;
import com.jobby.domain.mobility.error.Field;
import com.jobby.domain.mobility.result.Result;
import com.jobby.userservice.domain.commands.UploadImageCommand;
import org.mapstruct.Mapper;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Mapper(componentModel = "spring")
public interface UserHttpMapper {
    default Result<UploadImageCommand, Error> toUploadImageCommand(long id, MultipartFile file){
        try {
            var response = new UploadImageCommand(id,
                    file.getOriginalFilename(),
                    file.getBytes(),
                    file.getSize(),
                    file.getContentType());
            return Result.success(response);
        } catch (IOException e) {
            return Result.failure(ErrorType.INVALID_INPUT, new Field("file", "The file cannot be accessed"));
        }
    }
}
