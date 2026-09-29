package GUI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

/**
 * Muestra los paneles de administración como "ventanas" superpuestas dentro
 * del mismo JFrame (no se abre ninguna ventana/JFrame nueva). Se agregan al
 * JLayeredPane del frame, sobre una capa que oscurece el fondo.
 *
 * Reglas que implementa:
 *  - La ventana queda sobrepuesta en el mismo JFrame de donde se abrió.
 *  - Si se hace click fuera del panel de contenido, se cierra sola.
 *  - El propio panel de contenido (ver {@link PanelAdminBase}) trae un
 *    único botón de cerrar.
 *
 * Todo escrito a mano, sin usar el diseñador de NetBeans.
 */
public final class OverlayManager {

    private OverlayManager() {
    }

    /** Muestra 'contenido' superpuesto y centrado sobre 'ventana'. */
    public static void mostrar(JFrame ventana, JComponent contenido) {
        JLayeredPane capas = ventana.getLayeredPane();

        JPanel fondo = new JPanel(null);
        fondo.setOpaque(true);
        fondo.setBackground(new Color(0, 0, 0, 140));
        fondo.setBounds(0, 0, capas.getWidth(), capas.getHeight());

        Dimension pref = contenido.getPreferredSize();
        int ancho = Math.max(pref.width, 1000);
        int alto = Math.max(pref.height, 650);
        ubicarCentrado(fondo, contenido, ancho, alto);
        fondo.add(contenido);

        // Cerrar al hacer click en cualquier parte del fondo que NO sea el
        // panel de contenido (los clicks sobre 'contenido' no llegan aquí
        // porque 'contenido' es opaco y los intercepta primero).
        MouseAdapter cerrarAlClickAfuera = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cerrar(ventana, fondo);
            }
        };
        fondo.addMouseListener(cerrarAlClickAfuera);

        // Si la ventana cambia de tamaño (maximizar, resize) reacomodar todo.
        ComponentAdapter listenerResize = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                fondo.setBounds(0, 0, capas.getWidth(), capas.getHeight());
                ubicarCentrado(fondo, contenido, ancho, alto);
                fondo.revalidate();
                fondo.repaint();
            }
        };
        capas.addComponentListener(listenerResize);
        fondo.putClientProperty("resizeListener", listenerResize);

        capas.add(fondo, JLayeredPane.POPUP_LAYER);
        capas.revalidate();
        capas.repaint();

        if (contenido instanceof PanelAdminBase) {
            ((PanelAdminBase) contenido).setAccionCerrar(() -> cerrar(ventana, fondo));
        }
    }

    private static void ubicarCentrado(JPanel fondo, JComponent contenido, int ancho, int alto) {
        int x = Math.max((fondo.getWidth() - ancho) / 2, 0);
        int y = Math.max((fondo.getHeight() - alto) / 2, 0);
        contenido.setBounds(x, y, ancho, alto);
    }

    /** Quita la ventana superpuesta del JFrame. */
    public static void cerrar(JFrame ventana, JPanel fondo) {
        JLayeredPane capas = ventana.getLayeredPane();
        Object listenerObj = fondo.getClientProperty("resizeListener");
        if (listenerObj instanceof ComponentAdapter) {
            capas.removeComponentListener((ComponentAdapter) listenerObj);
        }
        capas.remove(fondo);
        capas.revalidate();
        capas.repaint();
    }
}
