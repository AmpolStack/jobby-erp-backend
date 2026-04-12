package com.jobby.userservice.domain.ports;

import com.jobby.userservice.domain.models.ContactType;

import java.util.List;
import java.util.Optional;

public interface ContactTypeRepository {
    Optional<ContactType> findById(Integer id);
    List<ContactType> findAll();
    ContactType save(ContactType contactType);
    void deleteById(Integer id);
    boolean existsById(Integer id);
}
