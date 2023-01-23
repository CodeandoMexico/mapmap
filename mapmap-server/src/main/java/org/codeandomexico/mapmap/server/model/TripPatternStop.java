package org.codeandomexico.mapmap.server.model;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class TripPatternStop {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hibernate")
    @SequenceGenerator(name = "hibernate", sequenceName = "hibernate_sequence", allocationSize = 1)
    public Long id;

    @ManyToOne
    @JoinColumn(name = "pattern_id")
    public TripPattern tripPattern;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "stop_id")
    public Stop stop;

    public Integer stopSequence;
    public Double defaultDistance;
    public Integer defaultTravelTime;
    public Integer defaultDwellTime;
    public Integer board;
    public Integer alight;

    public TripPatternStop(TripPattern tripPattern, Stop stop, Integer stopSequence, Integer defaultTravelTime) {
        this.tripPattern = tripPattern;
        this.stop = stop;
        this.stopSequence = stopSequence;
        this.defaultTravelTime = defaultTravelTime;
        this.defaultDistance = 0.0;
    }
}


