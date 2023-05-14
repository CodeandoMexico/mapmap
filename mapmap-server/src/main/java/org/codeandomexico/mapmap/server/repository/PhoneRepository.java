package org.codeandomexico.mapmap.server.repository;

import org.codeandomexico.mapmap.server.model.Phone;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PhoneRepository extends CrudRepository<Phone, Long> {
    Optional<Phone> findFirstByUnitId(String unitId);

    Optional<Phone> findByImei(String imei);

    Optional<Phone> findFirstByImei(String imei);

    Optional<Phone> findByUnitId(String unitId);

    long countByUnitId(String unitId);
}
