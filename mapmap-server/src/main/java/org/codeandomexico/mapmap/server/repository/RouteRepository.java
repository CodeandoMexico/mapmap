package org.codeandomexico.mapmap.server.repository;

import org.codeandomexico.mapmap.server.model.Route;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteRepository extends CrudRepository<Route, Long> {

}
