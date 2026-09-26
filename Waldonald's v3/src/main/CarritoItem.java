package main;

/**
 * Un renglón dentro del carrito de compras: un producto y la cantidad
 * acumulada de veces que se agregó.
 */
public class CarritoItem {

    private final int idProducto;
    private final String nombre;
    private final double precioUnitario;
    private int cantidad;

    public CarritoItem(int idProducto, String nombre, double precioUnitario) {
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.precioUnitario = precioUnitario;
        this.cantidad = 1;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public String getNombre() {
        return nombre;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void incrementarCantidad() {
        this.cantidad++;
    }

    public double getSubtotal() {
        return precioUnitario * cantidad;
    }

    @Override
    public String toString() {
        return String.format("%-28s x%-3d Q%.2f", nombre, cantidad, getSubtotal());
    }
}
