package com.podStream.PodStream.Models;

/**
 * La enumeración CategoryProduct define las diferentes categorías de productos disponibles en la tienda.
 * Esto facilita la clasificación de los productos por tipo.
 *
 * Ejemplo de uso:
 * Product product = new Product("Micrófono", "Micrófono profesional", 150.0, 10, CategoryProduct.MICROPHONES, color, 0.0, "image.jpg");
 */
public enum CategoryProduct {

    /**
     * Productos relacionados con micrófonos, incluyendo micrófonos de condensador, dinámicos y accesorios.
     */
    MICROPHONES,

    /**
     * Productos relacionados con consolas de sonido, como mezcladoras y interfaces de audio.
     */
    SOUND_CONSOLES,

    /**
     * Cámaras utilizadas para streaming y grabación de video.
     */
    CAMERAS,

    /**
     * Tarjetas de sonido externas o internas utilizadas para mejorar la calidad de audio.
     */
    SOUND_CARDS,

    /**
     * Accesorios para grabación y streaming, como trípodes, cables y soportes.
     */
    ACCESSORIES
}
