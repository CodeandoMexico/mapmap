package org.codeandomexico.mapmap.server.repository;

import org.codeandomexico.mapmap.server.model.Phone;
import org.codeandomexico.mapmap.server.model.TripPattern;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface TripPatternRepository extends CrudRepository<TripPattern, Long> {
    List<TripPattern> findByRoute_Phone_UnitIdInOrderByIdAsc(Collection<String> unitIds);

    List<TripPattern> findByRoute_PhoneOrderByIdDesc(Phone phone);
}
