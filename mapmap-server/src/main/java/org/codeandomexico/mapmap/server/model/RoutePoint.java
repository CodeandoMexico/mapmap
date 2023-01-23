package org.codeandomexico.mapmap.server.model;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class RoutePoint {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hibernate")
    @SequenceGenerator(name = "hibernate", sequenceName = "hibernate_sequence", allocationSize = 1)
    public Long id;

    @ManyToOne
    @JoinColumn(name = "route_id")
    public Route route;

    public Double lat;
    public Double lon;
    public Integer sequence;
    public Integer timeOffset;

}


