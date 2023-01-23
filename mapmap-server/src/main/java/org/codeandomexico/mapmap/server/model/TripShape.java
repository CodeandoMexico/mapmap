package org.codeandomexico.mapmap.server.model;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.LineString;

@Entity
@NoArgsConstructor
public class TripShape {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hibernate")
    @SequenceGenerator(name = "hibernate", sequenceName = "hibernate_sequence", allocationSize = 1)
    public Long id;
    public String gtfsShapeId;
    public String description;
    public Double describedDistance;
    @Column(columnDefinition = "geometry")
    public LineString shape;
    @Column(columnDefinition = "geometry")
    public LineString simpleShape;
    @OneToOne(mappedBy = "tripShape")
    public TripPattern tripPattern;

}
