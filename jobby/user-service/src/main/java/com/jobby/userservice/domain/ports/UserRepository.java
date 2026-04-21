package com.jobby.userservice.domain.ports;

import com.jobby.userservice.domain.models.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(Long id);
    List<User> findAll();
    User save(User user);
    void deleteById(Long id);
    boolean existsById(Long id);
}
