package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Panel base para todas las "ventanas" superpuestas del Administrador.
 * Da un encabezado con título y UN único botón de cerrar; las subclases
 * solo tienen que construir su contenido dentro de {@link #panelContenido}.
 *
 * Escrito 100% a código (sin diseñador de NetBeans).
 */
public abstract class PanelAdminBase extends JPanel {

    protected final JPanel panelContenido;
    private Runnable accionCerrar;

    public PanelAdminBase(String titulo) {
        super(new BorderLayout());
        setBackground(new Color(250, 247, 240));
        setBorder(BorderFactory.createLineBorder(new Color(209, 146, 51), 3));

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(new Color(20, 20, 20));
        encabezado.setPreferredSize(new Dimension(10, 54));

        JLabel lblTitulo = new JLabel("   " + titulo);
        lblTitulo.setForeground(new Color(209, 146, 51));
        lblTitulo.setFont(new Font("Segoe UI Semibold", Font.BOLD, 20));
        encabezado.add(lblTitulo, BorderLayout.WEST);

        JButton btnCerrar = new JButton("Cerrar  ✕");
        btnCerrar.setBackground(new Color(160, 38, 35));
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.setFocusPainted(false);
        btnCerrar.setFont(new Font("Dialog", Font.BOLD, 14));
        btnCerrar.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        btnCerrar.addActionListener(e -> cerrarAhora());

        JPanel wrapperBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        wrapperBtn.setOpaque(false);
        wrapperBtn.add(btnCerrar);
        encabezado.add(wrapperBtn, BorderLayout.EAST);

        add(encabezado, BorderLayout.NORTH);

        panelContenido = new JPanel();
        panelContenido.setBackground(new Color(250, 247, 240));
        add(panelContenido, BorderLayout.CENTER);
    }

    /** Llamado por OverlayManager al mostrar el panel. */
    void setAccionCerrar(Runnable accion) {
        this.accionCerrar = accion;
    }

    /** Las subclases pueden llamar esto (ej. tras guardar) para cerrarse solas. */
    protected void cerrarAhora() {
        if (accionCerrar != null) {
            accionCerrar.run();
        }
    }
}
