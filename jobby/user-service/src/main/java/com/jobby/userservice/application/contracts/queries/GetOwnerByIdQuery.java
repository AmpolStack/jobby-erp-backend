package com.jobby.userservice.application.contracts.queries;

import com.jobby.domain.ports.in.Query;

public record GetOwnerByIdQuery(long ownerId) implements Query {
}
