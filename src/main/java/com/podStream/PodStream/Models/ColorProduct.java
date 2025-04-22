package com.podStream.PodStream.Models;

/**
 * La enumeración ColorProduct define los colores disponibles para los productos en la tienda.
 * Esto ayuda a los usuarios a seleccionar productos basados en el color.
 *
 * Ejemplo de uso:
 * Product product = new Product("Micrófono", "Micrófono profesional", 150.0, 10, category, ColorProduct.BLACK, 0.0, "image.jpg");
 */
public enum ColorProduct {

    /**
     * Color negro.
     */
    BLACK,

    /**
     * Color blanco.
     */
    WHITE,

    /**
     * Color gris.
     */
    GRAY,

    /**
     * Color rojo.
     */
    RED,

    /**
     * Color azul.
     */
    BLUE,

    /**
     * Color verde.
     */
    GREEN
}
