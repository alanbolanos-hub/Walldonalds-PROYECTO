package main;

/**
 * Representa un producto leído desde la tabla Productos de la base de datos.
 * Solo se usa como resultado de consultas; no se persiste directamente.
 */
public class ProductoDB {

    private final int idProducto;
    private final String nombre;
    private final double precio;

    public ProductoDB(int idProducto, String nombre, double precio) {
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.precio = precio;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public String getNombre() {
        return nombre;
    }

    public double getPrecio() {
        return precio;
    }
}
