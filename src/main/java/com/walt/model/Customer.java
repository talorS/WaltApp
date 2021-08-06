package com.walt.model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class Customer extends  NamedEntity{

    @ManyToOne
    City city;
    String address;

    public Customer(){}

    public Customer(final String name,final City city,final String address) {
        super(name);
        this.city = city;
        this.address = address;
    }

    public City getCity() {
        return city;
    }

    public void setCity(final City city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(final String address) {
        this.address = address;
    }
}
