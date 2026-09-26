package GUI;

import main.Carrito;
import main.CarritoItem;
import main.Conexion;
import main.NoHayStockException;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.border.EmptyBorder;
import java.util.List;


public class CarritoPanel extends JPanel implements Carrito.CarritoListener {

    private static final int ANCHO_COLAPSADO = 150;
    private static final int ALTO_COLAPSADO = 34;
    private static final int ANCHO_EXPANDIDO = 300;
    private static final int ALTO_EXPANDIDO = 380;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contenedor = new JPanel(cardLayout);

    private final DefaultListModel<String> modeloLista = new DefaultListModel<>();
    private final JList<String> listaCarrito = new JList<>(modeloLista);
    private final JLabel labelTotal = new JLabel("Total: Q0.00");
    private final JLabel labelResumenColapsado = new JLabel();
    private final JButton botonComprar = new JButton("Comprar");
    private final JButton botonCancelar = new JButton("Cancelar orden");

    private boolean expandido = false;
    private JFrame frameAsociado;

    public CarritoPanel() {
        setLayout(new BorderLayout());
        setOpaque(false);
        add(contenedor, BorderLayout.CENTER);

        contenedor.add(construirBarraColapsada(), "colapsado");
        contenedor.add(construirPanelExpandido(), "expandido");

        Carrito.getInstancia().agregarListener(this);
        actualizarLista();
    }

    // ---------------------------------------------------------------
    // Barra colapsada: una sola línea, como en el boceto marcado en rojo.
    // ---------------------------------------------------------------
    private JPanel construirBarraColapsada() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(new Color(30, 30, 30));
        barra.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));
        barra.setPreferredSize(new Dimension(ANCHO_COLAPSADO, ALTO_COLAPSADO));

        labelResumenColapsado.setForeground(Color.WHITE);
        labelResumenColapsado.setFont(new Font("Segoe UI Semibold", Font.BOLD, 12));
        labelResumenColapsado.setBorder(new EmptyBorder(0, 10, 0, 0));

        JButton botonExpandir = crearBotonIcono("▲");
        botonExpandir.addActionListener(e -> expandir());

        barra.add(labelResumenColapsado, BorderLayout.CENTER);
        barra.add(botonExpandir, BorderLayout.EAST);
        barra.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                expandir();
            }
        });
        return barra;
    }

    // ---------------------------------------------------------------
    // Panel expandido: pequeño, no ocupa toda la pantalla.
    // ---------------------------------------------------------------
    private JPanel construirPanelExpandido() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(150, 150, 150)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        panel.setPreferredSize(new Dimension(ANCHO_EXPANDIDO, ALTO_EXPANDIDO));

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setOpaque(false);
        JLabel titulo = new JLabel("Carrito de compras", SwingConstants.LEFT);
        titulo.setFont(new Font("Segoe UI Semibold", Font.BOLD, 18));
        JButton botonMinimizar = crearBotonIcono("—");
        botonMinimizar.addActionListener(e -> colapsar());
        encabezado.add(titulo, BorderLayout.CENTER);
        encabezado.add(botonMinimizar, BorderLayout.EAST);
        panel.add(encabezado, BorderLayout.NORTH);

        panel.add(new JScrollPane(listaCarrito), BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new BorderLayout(5, 5));
        panelInferior.setOpaque(false);
        labelTotal.setFont(new Font("Segoe UI", Font.BOLD, 16));
        panelInferior.add(labelTotal, BorderLayout.NORTH);

        JPanel panelBotones = new JPanel(new GridLayout(1, 2, 10, 0));
        panelBotones.setOpaque(false);
        botonComprar.setBackground(new Color(46, 139, 87));
        botonComprar.setForeground(Color.WHITE);
        botonCancelar.setBackground(new Color(204, 0, 0));
        botonCancelar.setForeground(Color.WHITE);
        panelBotones.add(botonComprar);
        panelBotones.add(botonCancelar);
        panelInferior.add(panelBotones, BorderLayout.SOUTH);

        panel.add(panelInferior, BorderLayout.SOUTH);

        botonComprar.addActionListener(e -> comprar());
        botonCancelar.addActionListener(e -> cancelarOrden());

        return panel;
    }

    private JButton crearBotonIcono(String texto) {
        JButton boton = new JButton(texto);
        boton.setMargin(new Insets(0, 6, 0, 6));
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setContentAreaFilled(false);
        boton.setForeground(Color.DARK_GRAY);
        return boton;
    }

    // ---------------------------------------------------------------
    // Colapsar / expandir (como doblar/desdoblar un papel).
    // ---------------------------------------------------------------
    public void colapsar() {
        expandido = false;
        cardLayout.show(contenedor, "colapsado");
        ajustarTamano();
    }

    public void expandir() {
        expandido = true;
        cardLayout.show(contenedor, "expandido");
        ajustarTamano();
    }

    private void ajustarTamano() {
        Dimension tam = expandido
                ? new Dimension(ANCHO_EXPANDIDO, ALTO_EXPANDIDO)
                : new Dimension(ANCHO_COLAPSADO, ALTO_COLAPSADO);
        setPreferredSize(tam);
        setSize(tam);
        reposicionar();
        revalidate();
        repaint();
    }

    // ---------------------------------------------------------------
    // Anclarlo dentro de un JFrame, pegado a la esquina superior derecha.
    // Reemplaza al antiguo abrirVentanaCarrito().
    // ---------------------------------------------------------------
    public void anclarEn(JFrame frame) {
        this.frameAsociado = frame;
        frame.getLayeredPane().add(this, JLayeredPane.PALETTE_LAYER);
        colapsar();
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                reposicionar();
            }
        });
    }

    private void reposicionar() {
        if (frameAsociado == null) {
            return;
        }
        int margen = 15;
        int x = frameAsociado.getContentPane().getWidth() - getPreferredSize().width - margen;
        setBounds(x, margen, getPreferredSize().width, getPreferredSize().height);
    }

    // ---------------------------------------------------------------
    // Acciones (idénticas a las que ya tenías).
    // ---------------------------------------------------------------
    private void comprar() {
        List<CarritoItem> items = Carrito.getInstancia().getItems();
        if (items.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El carrito está vacío.",
                    "Carrito vacío", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Estás seguro de realizar esta compra?",
                "Confirmar compra", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            Conexion.procesarCompra(items, Carrito.getInstancia().getTotal());
            JOptionPane.showMessageDialog(this, "¡Compra realizada con éxito!",
                    "Compra completada", JOptionPane.INFORMATION_MESSAGE);
            Carrito.getInstancia().vaciar();
            colapsar();
        } catch (NoHayStockException ex) {
            JOptionPane.showMessageDialog(this,
                    "No hay suficiente stock de \"" + ex.getNombreProducto() + "\" (disponible: "
                    + ex.getStockDisponible() + ").\nAjusta la cantidad o quita ese producto del carrito.",
                    "Stock insuficiente", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelarOrden() {
        if (Carrito.getInstancia().estaVacio()) {
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Deseas cancelar y vaciar el carrito?",
                "Cancelar orden", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (respuesta == JOptionPane.YES_OPTION) {
            Carrito.getInstancia().vaciar();
        }
    }

    @Override
    public void onCarritoActualizado() {
        actualizarLista();
    }

    private void actualizarLista() {
        modeloLista.clear();
        List<CarritoItem> items = Carrito.getInstancia().getItems();
        for (CarritoItem item : items) {
            modeloLista.addElement(item.toString());
        }
        String total = String.format("Q%.2f", Carrito.getInstancia().getTotal());
        labelTotal.setText("Total: " + total);
        labelResumenColapsado.setText("🛒 (" + items.size() + ") " + total);
        if (frameAsociado != null) {
            reposicionar();
        }
    }
}