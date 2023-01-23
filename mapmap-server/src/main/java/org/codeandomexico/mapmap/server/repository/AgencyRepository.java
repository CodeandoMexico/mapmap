package org.codeandomexico.mapmap.server.repository;

import org.codeandomexico.mapmap.server.model.Agency;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgencyRepository extends CrudRepository<Agency, Long> {

}
