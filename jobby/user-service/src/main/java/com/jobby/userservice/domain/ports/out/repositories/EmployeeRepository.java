package com.jobby.userservice.domain.ports.out.repositories;

import com.jobby.userservice.domain.models.aggregate.Employee;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository {
    Optional<Employee> findById(Long id);
    List<Employee> findAll();
    Employee save(Employee employee);
    void deleteById(Long id);
    boolean existsById(Long id);
    Optional<Employee> findByUserId(Long userId);
}
