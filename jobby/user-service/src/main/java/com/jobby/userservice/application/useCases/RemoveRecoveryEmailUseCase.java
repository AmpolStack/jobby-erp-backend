package com.jobby.userservice.application.useCases;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.domain.ports.TransactionOrchestrator;
import com.jobby.userservice.application.mapper.GetOwnerQueryMapper;
import com.jobby.userservice.application.queries.GetOwnerQuery;
import com.jobby.userservice.domain.models.Owner;
import com.jobby.userservice.domain.ports.out.repositories.models.OwnerRepository;
import com.jobby.userservice.domain.ports.out.repositories.models.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RemoveRecoveryEmailUseCase {

    private final OwnerRepository ownerRepository;
    private final UserRepository userRepository;
    private final TransactionOrchestrator transaction;
    private final GetOwnerQueryMapper mapper;

    public final Result<GetOwnerQuery, Error> execute(long id){
        return this.ownerRepository.getById(id)
                .peek(Owner::removeRecoveryEmail)
                .flatMap(owner -> this.ownerRepository.prepareSave(owner)
                        .flatMap(task -> this.transaction.write()
                                .add(task)
                                .build())
                        .flatMap(v -> this.userRepository.getById(owner.getUserId()))
                        .map(user -> this.mapper.toGetOwnerQuery(owner, user))
                );
    }
}
