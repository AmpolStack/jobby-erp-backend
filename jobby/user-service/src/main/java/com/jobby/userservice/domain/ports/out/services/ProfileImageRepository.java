package com.jobby.userservice.domain.ports.out.services;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.userservice.domain.vo.ImageUrl;
import com.jobby.userservice.domain.vo.ProfileImage;

public interface ProfileImageRepository {
    Result<ImageUrl, Error> save(ProfileImage image);
    Result<Void, Error> remove(ImageUrl url);
}
