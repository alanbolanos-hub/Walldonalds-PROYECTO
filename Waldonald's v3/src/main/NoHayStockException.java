package main;

/**
 * Se lanza cuando al procesar una compra algún producto ya no tiene
 * stock suficiente en Inventario.
 */
public class NoHayStockException extends Exception {

    private final String nombreProducto;
    private final int stockDisponible;

    public NoHayStockException(String nombreProducto, int stockDisponible) {
        super("Stock insuficiente para \"" + nombreProducto + "\" (disponible: " + stockDisponible + ")");
        this.nombreProducto = nombreProducto;
        this.stockDisponible = stockDisponible;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public int getStockDisponible() {
        return stockDisponible;
    }
}
