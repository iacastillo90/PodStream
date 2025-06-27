package com.podStream.PodStream.Models;

public enum InteractionType {

    VIEW,            // Vista de producto
    ADD_TO_CART,     // Añadir al carrito
    PURCHASE,        // Compra finalizada (la más importante)
    RATING,          // Calificación explícita
    CLICK,           // Clic en un enlace de producto
    SEARCH           // Búsqueda de producto

}
