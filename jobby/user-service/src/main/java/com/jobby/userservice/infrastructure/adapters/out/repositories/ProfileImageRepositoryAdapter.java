package com.jobby.userservice.infrastructure.adapters.out.repositories;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.infrastructure.adapter.FileStorageServiceAdapter;
import com.jobby.userservice.domain.ports.out.services.ProfileImageRepository;
import com.jobby.userservice.domain.vo.ImageUrl;
import com.jobby.userservice.domain.vo.ProfileImage;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class ProfileImageRepositoryAdapter implements ProfileImageRepository {

    private final FileStorageServiceAdapter fileStorageService;

    @Override
    public Result<ImageUrl, Error> save(ProfileImage image) {
        return this.fileStorageService.upload(image.getContent(), image.getMimeType(), image.getKey())
                .flatMap(ImageUrl::of);
    }

    @Override
    public Result<Void, Error> remove(ImageUrl url) {
        var key = this.fileStorageService.extractKeyFromUrl(url.getValue());
        return this.fileStorageService.delete(key);
    }
}
