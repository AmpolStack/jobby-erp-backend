package com.jobby.userservice.application.useCases;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.userservice.application.commands.CreateOwnerCommand;
import com.jobby.userservice.application.mapper.GetOwnerQueryMapper;
import com.jobby.userservice.application.responses.GetOwnerQuery;
import com.jobby.userservice.domain.factory.OwnerFactory;
import com.jobby.userservice.domain.factory.UserFactory;
import com.jobby.userservice.domain.models.IdentificationType;
import com.jobby.userservice.domain.ports.OwnerRepository;
import org.springframework.stereotype.Service;
import java.util.Set;

@Service
public class CreateOwnerUseCase {

    private final OwnerRepository repository;
    private final GetOwnerQueryMapper ownerQueryMapper;

    public CreateOwnerUseCase(OwnerRepository repository,GetOwnerQueryMapper ownerQueryMapper) {
        this.repository = repository;
        this.ownerQueryMapper = ownerQueryMapper;
    }

    public Result<GetOwnerQuery, Error> execute(CreateOwnerCommand command){
        var inputUser = command.getUser();

        return UserFactory.create(1L, 1, inputUser.getFirstName(),
                inputUser.getFirstName(), "owner",  inputUser.getIdentificationNumber(),
                new IdentificationType(1, 1, "CC", 5, 10, "^\\d+$", "CC", Set.of("numbers")),
                inputUser.getEmail(), inputUser.getPhone())
                .flatMap((user) -> OwnerFactory.create(1L, null, command.getSecureParameters(), user))
                .flatMap(this.repository::create)
                .map(this.ownerQueryMapper::toGetOwnerQuery);

    }
}
