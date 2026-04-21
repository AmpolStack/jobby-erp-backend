package com.jobby.userservice.application.mapper;

import com.jobby.domain.mobility.error.Error;
import com.jobby.domain.mobility.result.Result;
import com.jobby.userservice.application.commands.CreateOwnerCommand;
import com.jobby.userservice.domain.models.Owner;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CreateOwnerCommandMapper {
    default Result<Owner, Error> toOwner(CreateOwnerCommand command,
                                         long ownerId,
                                         long userId) {
        return Owner.create(ownerId, userId, command.getSecureParameters());
    }
}
