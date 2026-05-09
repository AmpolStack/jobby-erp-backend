package com.jobby.userservice.infrastructure.adapters.out.services;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.userservice.domain.ports.out.services.FileStorageService;
import com.jobby.userservice.domain.models.vo.shared.ImageUrl;
import com.jobby.userservice.domain.models.vo.ephemeral.ProfileImage;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FileStorageServiceAdapter implements FileStorageService {

    private final com.jobby.infrastructure.adapter.FileStorageServiceAdapter fileStorageService;

    @Override
    public Result<ImageUrl, Error> changeProfileImage(ProfileImage image) {
        return this.fileStorageService.upload(image.getContent(), image.getMimeType(), image.getKey())
                .flatMap(ImageUrl::of);
    }

    @Override
    public Result<Void, Error> removeProfileImage(ImageUrl url) {
        var key = this.fileStorageService.extractKeyFromUrl(url.getValue());
        return this.fileStorageService.delete(key);
    }
}
