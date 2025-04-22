package com.podStream.PodStream.Models;

/**
 * La enumeración PersonType define los diferentes tipos de personas o usuarios en el sistema.
 * Esto ayuda a distinguir entre administradores y clientes, asignándoles permisos y roles específicos.
 *
 * Ejemplo de uso:
 * PersonType userType = PersonType.CLIENT;
 */
public enum PersonType {

    /**
     * Usuario con privilegios administrativos, con acceso a gestionar el sistema.
     */
    ADMIN,

    /**
     * Usuario que es un cliente del sistema, con privilegios de compra.
     */
    CLIENT
}
