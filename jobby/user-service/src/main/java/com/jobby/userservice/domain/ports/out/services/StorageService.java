package com.jobby.userservice.domain.ports.out.services;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.userservice.domain.models.vo.shared.ImageUrl;
import com.jobby.userservice.domain.models.vo.ephemeral.ProfileImage;

public interface StorageService {
    Result<ImageUrl, Error> changeProfileImage(ProfileImage image);
    Result<Void, Error> removeProfileImage(ImageUrl url);
}
