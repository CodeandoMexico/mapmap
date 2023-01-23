package org.codeandomexico.mapmap.server.model;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class Agency {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hibernate")
    @SequenceGenerator(name = "hibernate", sequenceName = "hibernate_sequence", allocationSize = 1)
    public Long id;
    public String gtfsAgencyId;
    public String name;
    public String url;
    public String timezone;
    public String lang;
    public String phone;
    public String color;
    public Boolean systemMap;

    public Agency(org.onebusaway.gtfs.model.Agency agency) {
        this.gtfsAgencyId = agency.getId();
        this.name = agency.getName();
        this.url = agency.getUrl();
        this.timezone = agency.getTimezone();
        this.lang = agency.getLang();
        this.phone = agency.getPhone();
    }

    public Agency(String gtfsAgencyId, String name, String url, String timezone, String lang, String phone) {
        this.gtfsAgencyId = gtfsAgencyId;
        this.name = name;
        this.url = url;
        this.timezone = timezone;
        this.lang = lang;
        this.phone = phone;
    }

}
