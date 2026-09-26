package main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Carrito de compras en memoria, compartido por toda la aplicación
 * (una sola instancia, sin importar desde qué pantalla/JFrame se agregue
 * un producto). No toca la base de datos: eso ocurre solo al comprar,
 * ver {@link Conexion#procesarCompra(List, double)}.
 */
public class Carrito {

    /** Se notifica cada vez que el contenido del carrito cambia. */
    public interface CarritoListener {
        void onCarritoActualizado();
    }

    private static Carrito instancia;

    private final List<CarritoItem> items = new ArrayList<>();
    private final List<CarritoListener> listeners = new ArrayList<>();

    private Carrito() {
    }

    public static synchronized Carrito getInstancia() {
        if (instancia == null) {
            instancia = new Carrito();
        }
        return instancia;
    }

    public void agregarListener(CarritoListener listener) {
        listeners.add(listener);
    }

    public void quitarListener(CarritoListener listener) {
        listeners.remove(listener);
    }

    private void notificarCambio() {
        for (CarritoListener l : listeners) {
            l.onCarritoActualizado();
        }
    }

    /** Agrega un producto; si ya estaba en el carrito, solo incrementa la cantidad. */
    public synchronized void agregarProducto(int idProducto, String nombre, double precio) {
        for (CarritoItem item : items) {
            if (item.getIdProducto() == idProducto) {
                item.incrementarCantidad();
                notificarCambio();
                return;
            }
        }
        items.add(new CarritoItem(idProducto, nombre, precio));
        notificarCambio();
    }

    public synchronized void quitarProducto(int index) {
        if (index >= 0 && index < items.size()) {
            items.remove(index);
            notificarCambio();
        }
    }

    public synchronized void vaciar() {
        items.clear();
        notificarCambio();
    }

    public synchronized List<CarritoItem> getItems() {
        return Collections.unmodifiableList(new ArrayList<>(items));
    }

    public synchronized double getTotal() {
        double total = 0;
        for (CarritoItem item : items) {
            total += item.getSubtotal();
        }
        return total;
    }

    public synchronized boolean estaVacio() {
        return items.isEmpty();
    }
}
