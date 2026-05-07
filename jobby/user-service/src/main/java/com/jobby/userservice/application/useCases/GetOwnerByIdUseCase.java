package com.jobby.userservice.application.useCases;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.userservice.application.mapper.GetOwnerQueryMapper;
import com.jobby.userservice.application.queries.GetOwnerQuery;
import com.jobby.userservice.domain.ports.out.repositories.models.OwnerRepository;
import com.jobby.userservice.domain.ports.out.repositories.models.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GetOwnerByIdUseCase {

    private final UserRepository userRepository;
    private final OwnerRepository ownerRepository;
    private final GetOwnerQueryMapper mapper;

    public Result<GetOwnerQuery, Error> execute(long id){
        return this.ownerRepository.getById(id)
                .flatMap(owner ->
                        this.userRepository.getById(owner.getUserId())
                                .map(user -> this.mapper.toGetOwnerQuery(owner, user)));
    }
}
