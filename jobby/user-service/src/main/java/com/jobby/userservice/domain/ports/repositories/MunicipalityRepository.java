package com.jobby.userservice.domain.ports;

import com.jobby.userservice.domain.models.Municipality;

import java.util.List;
import java.util.Optional;

public interface MunicipalityRepository {
    Optional<Municipality> findById(Integer id);
    List<Municipality> findAll();
    Municipality save(Municipality municipality);
    void deleteById(Integer id);
    boolean existsById(Integer id);
    List<Municipality> findByDepartmentId(Integer departmentId);
}
