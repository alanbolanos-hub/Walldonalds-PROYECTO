package main;

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JOptionPane;
import javax.swing.Timer;

/**
 * Convierte cualquier JLabelEscalable de producto en un botón clicable:
 * cursor de mano, tooltip "¿Agregar al carrito?" al pasar el mouse, y al
 * hacer clic busca el producto en la base de datos (por nombre) y lo
 * agrega al carrito en memoria (main.Carrito).
 *
 * Uso, dentro del constructor de cada pantalla (después de initComponents()):
 *   BotonCarritoUtil.activarComoBotonCarrito(jLabelEscalable1, "Nombre exacto en Productos.nombre");
 */
public class BotonCarritoUtil {

    private BotonCarritoUtil() {
    }

    public static void activarComoBotonCarrito(JLabelEscalable label, String nombreProducto) {
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));

        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                label.setToolTipText("¿Agregar al carrito?");
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                ProductoDB producto = Conexion.buscarProductoPorNombre(nombreProducto);

                if (producto == null) {
                    JOptionPane.showMessageDialog(label,
                            "El producto \"" + nombreProducto + "\" no existe en la base de datos.\n"
                            + "Verifica que esté registrado en la tabla Productos con ese nombre exacto.",
                            "Producto no encontrado",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Carrito.getInstancia().agregarProducto(
                        producto.getIdProducto(), producto.getNombre(), producto.getPrecio());

                label.setToolTipText("¡Agregado al carrito!");
                Timer t = new Timer(1000, ev -> label.setToolTipText("¿Agregar al carrito?"));
                t.setRepeats(false);
                t.start();
            }
        });
    }
}
