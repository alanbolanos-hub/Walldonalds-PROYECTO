package GUI;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import main.Conexion;

/**
 * "Gestionar categorías": diagrama semi-visual de la base de datos, mostrando
 * la tabla Categorías en el centro y cada categoría alrededor, con la
 * cantidad de productos que contiene.
 */
public class PanelGestionarCategorias extends PanelAdminBase {

    public PanelGestionarCategorias() {
        super("Gestionar Categorías");
        construirContenido();
    }

    private void construirContenido() {
        panelContenido.setLayout(new BorderLayout(10, 10));
        panelContenido.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titulo = new JLabel("Diagrama de la base de datos — Categorías y cantidad de productos:");
        titulo.setFont(new Font("Dialog", Font.BOLD, 14));
        panelContenido.add(titulo, BorderLayout.NORTH);

        List<Conexion.CategoriaInfo> categorias = Conexion.listarCategorias();
        DiagramaCategorias diagrama = new DiagramaCategorias(categorias);
        diagrama.setPreferredSize(new Dimension(950, 650));

        JScrollPane scroll = new JScrollPane(diagrama);
        panelContenido.add(scroll, BorderLayout.CENTER);
    }

    /** Panel que dibuja el diagrama a mano usando Graphics2D. */
    private static class DiagramaCategorias extends JPanel {

        private final List<Conexion.CategoriaInfo> categorias;

        DiagramaCategorias(List<Conexion.CategoriaInfo> categorias) {
            this.categorias = categorias;
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int ancho = Math.max(getWidth(), 950);
            int alto = Math.max(getHeight(), 650);
            int cx = ancho / 2;
            int cy = alto / 2;

            // Nodo central: la base de datos.
            int centroAncho = 220;
            int centroAlto = 70;
            g2.setColor(new Color(20, 20, 20));
            g2.fillRoundRect(cx - centroAncho / 2, cy - centroAlto / 2, centroAncho, centroAlto, 18, 18);
            g2.setColor(new Color(209, 146, 51));
            g2.setFont(new Font("Segoe UI Semibold", Font.BOLD, 16));
            dibujarCentrado(g2, "WalDonals_db", cx, cy - 6);
            g2.setFont(new Font("Dialog", Font.PLAIN, 12));
            dibujarCentrado(g2, "(tabla Categorías)", cx, cy + 14);

            int n = categorias.size();
            if (n == 0) {
                g2.setColor(Color.GRAY);
                dibujarCentrado(g2, "No hay categorías registradas.", cx, cy + 100);
                return;
            }

            double radio = Math.min(ancho, alto) / 2.0 - 110;
            int cajaAncho = 190;
            int cajaAlto = 60;

            for (int i = 0; i < n; i++) {
                double angulo = (2 * Math.PI * i / n) - Math.PI / 2;
                int px = cx + (int) (radio * Math.cos(angulo));
                int py = cy + (int) (radio * Math.sin(angulo));

                // Línea de conexión al centro.
                g2.setColor(new Color(209, 146, 51));
                g2.setStroke(new BasicStroke(2f));
                g2.drawLine(cx, cy, px, py);

                Conexion.CategoriaInfo cat = categorias.get(i);
                int bx = px - cajaAncho / 2;
                int by = py - cajaAlto / 2;

                g2.setColor(new Color(250, 247, 240));
                g2.fillRoundRect(bx, by, cajaAncho, cajaAlto, 14, 14);
                g2.setColor(new Color(20, 20, 20));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(bx, by, cajaAncho, cajaAlto, 14, 14);

                g2.setFont(new Font("Dialog", Font.BOLD, 13));
                g2.setColor(new Color(20, 20, 20));
                dibujarCentrado(g2, cat.nombre, px, py - 8);
                g2.setFont(new Font("Dialog", Font.PLAIN, 12));
                g2.setColor(new Color(90, 90, 90));
                dibujarCentrado(g2, cat.cantidadProductos + " producto(s)", px, py + 12);
            }
        }

        private void dibujarCentrado(Graphics2D g2, String texto, int cx, int cy) {
            FontMetrics fm = g2.getFontMetrics();
            int x = cx - fm.stringWidth(texto) / 2;
            g2.drawString(texto, x, cy);
        }
    }
}
