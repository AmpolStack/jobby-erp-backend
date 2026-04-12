package com.jobby.userservice.domain.ports;

import com.jobby.userservice.domain.models.Owner;

import java.util.List;
import java.util.Optional;

public interface OwnerRepository {
    Optional<Owner> findById(Long id);
    List<Owner> findAll();
    Owner save(Owner owner);
    void deleteById(Long id);
    boolean existsById(Long id);
    Optional<Owner> findByUserId(Long userId);
}
