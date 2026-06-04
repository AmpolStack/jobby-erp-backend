package com.jobby.userservice.application.useCases;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.userservice.application.mappers.ResponseMapper;
import com.jobby.userservice.domain.contract.queries.GetOwnerByIdQuery;
import com.jobby.userservice.domain.contract.responses.OwnerResponse;
import com.jobby.userservice.domain.ports.in.GetOwnerByIdUseCase;
import com.jobby.userservice.domain.ports.out.repositories.OwnerRepository;
import com.jobby.userservice.domain.ports.out.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GetOwnerByIdUseCaseAdapter implements GetOwnerByIdUseCase {

    private final UserRepository userRepository;
    private final OwnerRepository ownerRepository;
    private final ResponseMapper responseMapper;

    @Override
    public Result<OwnerResponse, Error> execute(GetOwnerByIdQuery query){
        return this.ownerRepository.getById(query.ownerId())
                .flatMap(owner ->
                        this.userRepository.getById(owner.getUserId())
                                .map(user -> this.responseMapper.toResponse(owner, user)));
    }
}
