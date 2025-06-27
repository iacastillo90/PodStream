package com.podStream.PodStream.Models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.podStream.PodStream.Models.User.Client;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.HashSet;
import java.util.Set;

/**
 * La entidad Address representa una dirección física de envío o residencia.
 * Esta clase almacena los datos de la dirección asociados a una persona y a una orden de compra.
 *
 * Ejemplo de uso:
 * Address = new Address("Main Street", 123, "Madrid", "Apto 4B", 2, true, "28001", person);
 */
@Entity
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
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

    /**
     * Relación con la entidad Person.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    @JsonBackReference(value = "client-address") // Ignorar este lado de la relación
    private Client client;

    /**
     * Relación uno a uno con la orden de compra (PurchaseOrder).
     */
    @OneToMany(mappedBy = "address", fetch = FetchType.LAZY)
    @JsonManagedReference(value = "address-purchaseOrder") // Ignorar este lado de la relación
    private Set<PurchaseOrder> purchaseOrder = new HashSet<>();

    /**
     * Constructor vacío necesario para JPA.
     */
    public Address() {
    }

    /**
     * Constructor para crear una dirección con la información básica.
     *
     * @param street Calle de la dirección.
     * @param number Número de la dirección.
     * @param city Ciudad de la dirección.
     * @param apartament Apartamento, si aplica.
     * @param floor Piso, si aplica.
     * @param status Estado de la dirección (activa/inactiva).
     * @param zipCode Código postal.
     * @param client Persona asociada a la dirección.
     */
    public Address(String street, long number, String city, String apartament, long floor, boolean status, String zipCode, Client client) {
        this.street = street;
        this.number = number;
        this.city = city;
        this.apartament = apartament;
        this.floor = floor;
        this.status = status;
        this.zipCode = zipCode;
        this.client = client;
    }

    // Getters y Setters con Javadoc

    /**
     * Obtiene el ID de la dirección.
     *
     * @return ID de la dirección.
     */
    public long getId() {
        return id;
    }

    /**
     * Establece el ID de la dirección.
     *
     * @param id ID de la dirección.
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Obtiene la calle de la dirección.
     *
     * @return Calle de la dirección.
     */
    public String getStreet() {
        return street;
    }

    /**
     * Establece la calle de la dirección.
     *
     * @param street Calle de la dirección.
     */
    public void setStreet(String street) {
        this.street = street;
    }

    /**
     * Obtiene el número de la dirección.
     *
     * @return Número de la dirección.
     */
    public long getNumber() {
        return number;
    }

    /**
     * Establece el número de la dirección.
     *
     * @param number Número de la dirección.
     */
    public void setNumber(long number) {
        this.number = number;
    }

    /**
     * Obtiene la ciudad de la dirección.
     *
     * @return Ciudad de la dirección.
     */
    public String getCity() {
        return city;
    }

    /**
     * Establece la ciudad de la dirección.
     *
     * @param city Ciudad de la dirección.
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Obtiene el apartamento de la dirección.
     *
     * @return Apartamento de la dirección.
     */
    public String getApartament() {
        return apartament;
    }

    /**
     * Establece el apartamento de la dirección.
     *
     * @param apartament Apartamento de la dirección.
     */
    public void setApartament(String apartament) {
        this.apartament = apartament;
    }

    /**
     * Obtiene el piso de la dirección.
     *
     * @return Piso de la dirección.
     */
    public long getFloor() {
        return floor;
    }

    /**
     * Establece el piso de la dirección.
     *
     * @param floor Piso de la dirección.
     */
    public void setFloor(long floor) {
        this.floor = floor;
    }

    /**
     * Verifica si la dirección está activa o inactiva.
     *
     * @return Estado de la dirección.
     */
    public boolean isStatus() {
        return status;
    }

    /**
     * Establece el estado de la dirección.
     *
     * @param status Estado de la dirección (activa/inactiva).
     */
    public void setStatus(boolean status) {
        this.status = status;
    }

    /**
     * Obtiene el código postal de la dirección.
     *
     * @return Código postal.
     */
    public String getZipCode() {
        return zipCode;
    }

    /**
     * Establece el código postal de la dirección.
     *
     * @param zipCode Código postal.
     */
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    /**
     * Obtiene la persona asociada a la dirección.
     *
     * @return Persona asociada a la dirección.
     */
    public Client getClient() {
        return client;
    }

    /**
     * Establece la persona asociada a la dirección.
     *
     * @param client Persona asociada a la dirección.
     */
    public void setClient(Client client) {
        this.client = client;
    }

    /**
     * Obtiene la orden de compra asociada a la dirección.
     *
     * @return Orden de compra asociada a la dirección.
     */
    public Set<PurchaseOrder> getPurchaseOrder() {
        return purchaseOrder;
    }

    /**
     * Establece la orden de compra asociada a la dirección.
     *
     * @param purchaseOrder Orden de compra asociada a la dirección.
     */
    public void setPurchaseOrder(Set<PurchaseOrder> purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }
}
