package com.walt.model;

import javax.persistence.*;

@Entity
public class Driver extends NamedEntity {

    @ManyToOne
    City city;

    public Driver(){}

    public Driver(final String name,final City city){
        super(name);
        this.city = city;
    }

    public City getCity() {
        return city;
    }

    public void setCity(final City city) {
        this.city = city;
    }
}
