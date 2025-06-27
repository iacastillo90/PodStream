package com.podStream.PodStream.Models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.GenericGenerator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Representa un producto en el catálogo del e-commerce PodStream.
 *
 * Esta entidad almacena toda la información relevante de un producto,
 * incluyendo su nombre, descripción, precio, stock, y atributos
 * para la categorización. También gestiona relaciones con otras
 * entidades como comentarios, detalles de órdenes y puntuaciones.
 *
 * @author Iván Andrés Castillo Iligaray
 * @version 1.1
 * @since 2025-06-25
 */
@Entity
public class Product {

    /**
     * Identificador único del producto, generado automáticamente por la base de datos.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private Long id;

    /**
     * Nombre del producto. Es un campo obligatorio.
     * @see NotBlank
     * @see Size
     */
    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 2, max = 255, message = "El nombre debe tener entre 2 y 255 caracteres")
    private String name;

    /**
     * Descripción detallada del producto. Puede contener hasta 10,000 caracteres.
     */
    @Column(length = 10000)
    private String description;

    /**
     * Precio base del producto. Debe ser un valor positivo.
     */
    @Min(value = 0, message = "El precio no puede ser negativo")
    private double price;

    /**
     * Cantidad de unidades disponibles en inventario.
     */
    @Min(value = 0, message = "El stock no puede ser negativo")
    private int stock;

    /**
     * Contador de unidades vendidas. Utilizado para determinar la popularidad.
     * Se debe incrementar programáticamente cuando se completa una venta.
     */
    @Column(name = "sales_count")
    private int salesCount = 0;

    /**
     * Categoría a la que pertenece el producto.
     * @see CategoryProduct
     */
    @Enumerated(EnumType.STRING)
    private CategoryProduct category;

    /**
     * Color principal del producto.
     * @see ColorProduct
     */
    @Enumerated(EnumType.STRING)
    private ColorProduct color;

    /**
     * Porcentaje de descuento aplicado al producto (ej. 0.1 para 10%).
     */
    private double discount;

    /**
     * URL de la imagen principal y de portada del producto.
     */
    private String image;

    /**
     * Puntuación promedio del producto, calculada a partir de las calificaciones de los usuarios.
     */
    private double averagePoints;

    /**
     * Suma total de los puntos recibidos. Utilizado internamente para recalcular el promedio.
     */
    private double actuallyTotalPoints;

    /**
     * Colección de URLs de imágenes adicionales para la galería del producto.
     */
    @ElementCollection
    @CollectionTable(name = "product_image_collection", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    private List<String> imageCollection = new ArrayList<>();

    /**
     * Colección de puntuaciones individuales (ej. 1 a 5 estrellas) dadas por los usuarios.
     */
    @ElementCollection
    @CollectionTable(name = "product_points", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "point")
    private List<Integer> points = new ArrayList<>();

    /**
     * Relación con los detalles de las órdenes de compra en las que este producto ha sido incluido.
     * @see Details
     */
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private Set<Details> details = new HashSet<>();

    /**
     * Relación con los comentarios dejados por los usuarios en este producto.
     * Si se elimina el producto, sus comentarios también se eliminarán en cascada.
     * @see Comment
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private Set<ClientInteraction> clientInteractions = new HashSet<>();

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private Set<ProductRating> productRatings = new HashSet<>();

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private Set<CartItem> cartItems = new HashSet<>();

    /**
     * Constructor por defecto requerido por JPA.
     */
    public Product() { }

    /**
     * Constructor para crear una nueva instancia de Producto con los campos esenciales.
     *
     * @param name          El nombre del producto.
     * @param description   La descripción detallada.
     * @param price         El precio.
     * @param stock         La cantidad en inventario.
     * @param category      La categoría del producto.
     * @param color         El color del producto.
     * @param discount      El descuento a aplicar.
     * @param image         La URL de la imagen principal.
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

    // --- Getters y Setters ---

    /**
     * Obtiene el ID único del producto.
     * @return El ID del producto.
     */
    public Long getId() {
        return id;
    }

    /**
     * Establece el ID del producto.
     * @param id El nuevo ID del producto.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Obtiene el nombre del producto.
     * @return El nombre del producto.
     */
    public String getName() {
        return name;
    }

    /**
     * Establece el nombre del producto.
     * @param name El nuevo nombre del producto.
     */
    public void setName(String name) {
        this.name = name;
    }

    // ... (El resto de getters y setters seguirían este mismo patrón de documentación) ...

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    /**
     * Obtiene el contador de ventas del producto.
     * @return El número total de veces que este producto ha sido vendido.
     */
    public int getSalesCount() {
        return salesCount;
    }

    /**
     * Establece el contador de ventas del producto.
     * @param salesCount El nuevo total de ventas.
     */
    public void setSalesCount(int salesCount) {
        this.salesCount = salesCount;
    }

    public CategoryProduct getCategory() {
        return category;
    }

    public void setCategory(CategoryProduct category) {
        this.category = category;
    }

    public ColorProduct getColor() {
        return color;
    }

    public void setColor(ColorProduct color) {
        this.color = color;
    }

    public Set<ClientInteraction> getClientInteractions() {
        return clientInteractions;
    }

    public void setClientInteractions(Set<ClientInteraction> clientInteractions) {
        this.clientInteractions = clientInteractions;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public double getAveragePoints() {
        return averagePoints;
    }

    public void setAveragePoints(double averagePoints) {
        this.averagePoints = averagePoints;
    }

    public double getActuallyTotalPoints() {
        return actuallyTotalPoints;
    }

    public void setActuallyTotalPoints(double actuallyTotalPoints) {
        this.actuallyTotalPoints = actuallyTotalPoints;
    }

    public List<String> getImageCollection() {
        return imageCollection;
    }

    public void setImageCollection(List<String> imageCollection) {
        this.imageCollection = imageCollection;
    }

    public List<Integer> getPoints() {
        return points;
    }

    public void setPoints(List<Integer> points) {
        this.points = points;
    }

    public Set<Details> getDetails() {
        return details;
    }

    public void setDetails(Set<Details> details) {
        this.details = details;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public Set<ProductRating> getProductRatings() {
        return productRatings;
    }

    public void setProductRatings(Set<ProductRating> productRatings) {
        this.productRatings = productRatings;
    }

    public Set<CartItem> getCartItems() {
        return cartItems;
    }

    public void setCartItems(Set<CartItem> cartItems) {
        this.cartItems = cartItems;
    }
}
