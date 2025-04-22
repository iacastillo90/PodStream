package com.podStream.PodStream.Models;

import jakarta.persistence.*;
import org.antlr.v4.runtime.misc.NotNull;
import org.hibernate.annotations.GenericGenerator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * La entidad Product representa un producto en el sistema de comercio electrónico.
 * Se utiliza para almacenar información sobre los productos disponibles en la tienda.
 *
 * Ejemplo de uso:
 * Product producto = new Product("Micrófono", "Micrófono de alta calidad", 150.0, 10, category, color, 0.0, "imagen.jpg");
 */
@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    /**
     * Nombre del producto. No puede ser nulo y debe tener entre 2 y 255 caracteres.
     */
    @NotNull
    private String name;

    /**
     * Descripción detallada del producto. Puede tener hasta 10,000 caracteres.
     */
    @Column(length = 10000)
    private String description;

    /**
     * Precio del producto.
     */
    private double price;

    /**
     * Cantidad de stock disponible del producto.
     */
    private int stock;

    /**
     * Categoría a la que pertenece el producto.
     */
    private CategoryProduct category;

    /**
     * Color del producto.
     */
    private ColorProduct color;

    /**
     * Descuento aplicado al producto.
     */
    private double discount;

    /**
     * URL de la imagen principal del producto.
     */
    private String image;

    /**
     * Puntuación promedio del producto.
     */
    private double averagePoints;

    /**
     * Total de puntos actualmente acumulados para calcular la puntuación promedio.
     */
    private double actuallyTotalPoints;

    /**
     * Colección de URLs de imágenes adicionales del producto.
     */
    @ElementCollection
    private List<String> imageCollection = new ArrayList<>();

    /**
     * Colección de puntuaciones individuales del producto.
     */
    @ElementCollection
    private List<Integer> points = new ArrayList<>();

    /**
     * Relación uno a muchos con los detalles de las órdenes de compra.
     */
    @OneToMany(mappedBy = "product")
    private Set<Details> details = new HashSet<>();

    /**
     * Relación uno a muchos con los comentarios asociados al producto.
     * Se eliminan los comentarios si el producto es eliminado.
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    /**
     * Constructor vacío necesario para JPA.
     */
    public Product() { }

    /**
     * Constructor que permite crear un producto con información básica.
     *
     * @param name Nombre del producto.
     * @param description Descripción del producto.
     * @param price Precio del producto.
     * @param stock Stock disponible del producto.
     * @param category Categoría del producto.
     * @param color Color del producto.
     * @param discount Descuento aplicado al producto.
     * @param image URL de la imagen del producto.
     */
    public Product(String name, String description, double price, int stock, CategoryProduct category, ColorProduct color, double discount, String image) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.category = category;
        this.color = color;
        this.discount = discount;
        this.image = image;
    }

    /**
     * Obtiene el identificador único del producto.
     *
     * @return id del producto.
     */
    public long getId() {
        return id;
    }

    /**
     * Establece el identificador único del producto.
     *
     * @param id Identificador del producto.
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Obtiene el nombre del producto.
     *
     * @return Nombre del producto.
     */
    public String getName() {
        return name;
    }

    /**
     * Establece el nombre del producto.
     *
     * @param name Nombre del producto.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Obtiene la descripción del producto.
     *
     * @return Descripción del producto.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Establece la descripción del producto.
     *
     * @param description Descripción del producto.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Obtiene el precio del producto.
     *
     * @return Precio del producto.
     */
    public double getPrice() {
        return price;
    }

    /**
     * Establece el precio del producto.
     *
     * @param price Precio del producto.
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * Obtiene la cantidad de stock disponible del producto.
     *
     * @return Stock disponible.
     */
    public int getStock() {
        return stock;
    }

    /**
     * Establece la cantidad de stock disponible del producto.
     *
     * @param stock Cantidad de stock.
     */
    public void setStock(int stock) {
        this.stock = stock;
    }

    /**
     * Obtiene la categoría del producto.
     *
     * @return Categoría del producto.
     */
    public CategoryProduct getCategory() {
        return category;
    }

    /**
     * Establece la categoría del producto.
     *
     * @param category Categoría del producto.
     */
    public void setCategory(CategoryProduct category) {
        this.category = category;
    }

    /**
     * Obtiene el color del producto.
     *
     * @return Color del producto.
     */
    public ColorProduct getColor() {
        return color;
    }

    /**
     * Establece el color del producto.
     *
     * @param color Color del producto.
     */
    public void setColor(ColorProduct color) {
        this.color = color;
    }

    /**
     * Obtiene el descuento aplicado al producto.
     *
     * @return Descuento del producto.
     */
    public double getDiscount() {
        return discount;
    }

    /**
     * Establece el descuento aplicado al producto.
     *
     * @param discount Descuento aplicado al producto.
     */
    public void setDiscount(double discount) {
        this.discount = discount;
    }

    /**
     * Obtiene la URL de la imagen principal del producto.
     *
     * @return URL de la imagen.
     */
    public String getImage() {
        return image;
    }

    /**
     * Establece la URL de la imagen principal del producto.
     *
     * @param image URL de la imagen.
     */
    public void setImage(String image) {
        this.image = image;
    }

    /**
     * Obtiene el promedio de puntos del producto.
     *
     * @return Promedio de puntos.
     */
    public double getAveragePoints() {
        return averagePoints;
    }

    /**
     * Establece el promedio de puntos del producto.
     *
     * @param averagePoints Promedio de puntos.
     */
    public void setAveragePoints(double averagePoints) {
        this.averagePoints = averagePoints;
    }

    /**
     * Obtiene el total de puntos acumulados para el producto.
     *
     * @return Total de puntos acumulados.
     */
    public double getActuallyTotalPoints() {
        return actuallyTotalPoints;
    }

    /**
     * Establece el total de puntos acumulados para el producto.
     *
     * @param actuallyTotalPoints Total de puntos acumulados.
     */
    public void setActuallyTotalPoints(double actuallyTotalPoints) {
        this.actuallyTotalPoints = actuallyTotalPoints;
    }

    /**
     * Obtiene la colección de URLs de imágenes adicionales del producto.
     *
     * @return Lista de URLs de imágenes.
     */
    public List<String> getImageCollection() {
        return imageCollection;
    }

    /**
     * Establece la colección de imágenes adicionales del producto.
     *
     * @param imageCollection Lista de URLs de imágenes.
     */
    public void setImageCollection(List<String> imageCollection) {
        this.imageCollection = imageCollection;
    }

    /**
     * Obtiene la lista de puntuaciones del producto.
     *
     * @return Lista de puntuaciones.
     */
    public List<Integer> getPoints() {
        return points;
    }

    /**
     * Establece la lista de puntuaciones del producto.
     *
     * @param points Lista de puntuaciones.
     */
    public void setPoints(List<Integer> points) {
        this.points = points;
    }

    /**
     * Obtiene los detalles asociados al producto.
     *
     * @return Conjunto de detalles.
     */
    public Set<Details> getDetails() {
        return details;
    }

    /**
     * Establece los detalles asociados al producto.
     *
     * @param details Conjunto de detalles.
     */
    public void setDetails(Set<Details> details) {
        this.details = details;
    }

    /**
     * Obtiene los comentarios asociados al producto.
     *
     * @return Lista de comentarios.
     */
    public List<Comment> getComments() {
        return comments;
    }

    /**
     * Establece los comentarios asociados al producto.
     *
     * @param comments Lista de comentarios.
     */
    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
}
