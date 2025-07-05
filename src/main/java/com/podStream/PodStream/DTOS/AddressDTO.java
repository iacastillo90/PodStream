package com.podStream.PodStream.DTOS;

import com.podStream.PodStream.Models.Address;

public class AddressDTO {

    private long id;

    /**
     * Calle de la dirección.
     */
    private String street;

    /**
     * Número de la dirección.
     */
    private long number;

    /**
     * Ciudad de la dirección.
     */
    private String city;

    /**
     * Apartamento de la dirección, si corresponde.
     */
    private String apartament;

    /**
     * Piso de la dirección, si corresponde.
     */
    private long floor;

    /**
     * Estado de la dirección, puede ser activa o inactiva.
     */
    private boolean status;

    /**
     * Código postal de la dirección.
     */
    private String zipCode;

    public AddressDTO(Address address) {
        id = address.getId();
        street = address.getStreet();
        number = address.getNumber();
        city = address.getCity();
        apartament = address.getApartament();
        floor = address.getFloor();
        status = address.isStatus();
        zipCode = address.getZipCode();
    }

    public long getId() {
        return id;
    }

    public String getStreet() {
        return street;
    }

    public long getNumber() {
        return number;
    }

    public String getCity() {
        return city;
    }

    public String getApartament() {
        return apartament;
    }

    public long getFloor() {
        return floor;
    }

    public boolean isStatus() {
        return status;
    }

    public String getZipCode() {
        return zipCode;
    }
}
