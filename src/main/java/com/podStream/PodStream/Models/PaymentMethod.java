package com.podStream.PodStream.Models;

/**
 * La enumeración PaymentMethod representa los diferentes métodos de pago disponibles en la tienda.
 * Los clientes pueden pagar utilizando débito, crédito o efectivo.
 *
 * Ejemplo de uso:
 * PaymentMethod method = PaymentMethod.DEBIT;
 */
public enum PaymentMethod {

    /**
     * Pago con tarjeta de débito.
     */
    DEBIT,

    /**
     * Pago con tarjeta de crédito.
     */
    CREDIT,

    /**
     * Pago en efectivo.
     */
    CASH
}
