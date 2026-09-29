package GUI;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

import main.Conexion;

/**
 * "Registrar pedidos": arma un pedido agregando productos con cantidad y,
 * al finalizar, genera la factura (se guarda en Pedidos/Detalle_Pedidos y
 * se descuenta inventario).
 */
public class PanelRegistrarPedidos extends PanelAdminBase {

    private JComboBox<Conexion.ProductoInfo> comboProductos;
    private JSpinner spinnerCantidad;
    private JTextField txtCliente;
    private JComboBox<String> comboMetodoPago;
    private DefaultTableModel modeloCarrito;
    private JLabel lblTotal;

    private final List<Conexion.ItemPedidoNuevo> carrito = new ArrayList<>();

    public PanelRegistrarPedidos() {
        super("Registrar Pedidos");
        construirContenido();
    }

    @SuppressWarnings("unchecked")
    private void construirContenido() {
        panelContenido.setLayout(new BorderLayout(10, 10));
        panelContenido.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // ---- Panel superior: elegir producto + cantidad ----
        JPanel top = new JPanel(new GridBagLayout());
        top.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        List<Conexion.ProductoInfo> productos = Conexion.listarProductos(true);
        comboProductos = new JComboBox<>(productos.toArray(new Conexion.ProductoInfo[0]));
        comboProductos.setRenderer((lista, valor, idx, sel, foco) -> {
            JLabel l = new JLabel(valor == null ? "" : valor.nombre + " - Q" + String.format("%.2f", valor.precio)
                    + " (stock: " + valor.stock + ")");
            l.setOpaque(true);
            if (sel) {
                l.setBackground(lista.getSelectionBackground());
                l.setForeground(lista.getSelectionForeground());
            } else {
                l.setBackground(lista.getBackground());
                l.setForeground(lista.getForeground());
            }
            return l;
        });

        spinnerCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        JButton btnAgregar = new JButton("Agregar al pedido");
        btnAgregar.addActionListener(e -> agregarAlCarrito());

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        top.add(new JLabel("Producto:"), c);
        c.gridx = 1;
        c.weightx = 3;
        top.add(comboProductos, c);
        c.gridx = 2;
        c.weightx = 0;
        top.add(new JLabel("Cantidad:"), c);
        c.gridx = 3;
        top.add(spinnerCantidad, c);
        c.gridx = 4;
        top.add(btnAgregar, c);

        panelContenido.add(top, BorderLayout.NORTH);

        // ---- Tabla del pedido en construcción ----
        modeloCarrito = new DefaultTableModel(new Object[]{"Producto", "Cantidad", "Precio unit.", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        JTable tablaCarrito = new JTable(modeloCarrito);
        tablaCarrito.setRowHeight(24);
        panelContenido.add(new JScrollPane(tablaCarrito), BorderLayout.CENTER);

        // ---- Panel inferior: cliente, método de pago, total, botones ----
        JPanel bottom = new JPanel(new GridBagLayout());
        bottom.setOpaque(false);
        GridBagConstraints c2 = new GridBagConstraints();
        c2.insets = new Insets(4, 6, 4, 6);
        c2.fill = GridBagConstraints.HORIZONTAL;

        txtCliente = new JTextField("Cliente Mostrador");
        comboMetodoPago = new JComboBox<>(new String[]{"Efectivo", "Tarjeta", "Transferencia"});

        JButton btnQuitar = new JButton("Quitar seleccionado");
        btnQuitar.addActionListener(e -> quitarDeCarrito(tablaCarrito));

        JButton btnFactura = new JButton("Generar Factura");
        btnFactura.setFont(new Font("Dialog", Font.BOLD, 14));
        btnFactura.addActionListener(e -> generarFactura());

        lblTotal = new JLabel("Total: Q0.00");
        lblTotal.setFont(new Font("Dialog", Font.BOLD, 16));

        c2.gridx = 0;
        c2.gridy = 0;
        bottom.add(new JLabel("Cliente:"), c2);
        c2.gridx = 1;
        c2.weightx = 1;
        bottom.add(txtCliente, c2);
        c2.gridx = 2;
        c2.weightx = 0;
        bottom.add(new JLabel("Método de pago:"), c2);
        c2.gridx = 3;
        bottom.add(comboMetodoPago, c2);
        c2.gridx = 4;
        bottom.add(btnQuitar, c2);

        c2.gridx = 0;
        c2.gridy = 1;
        c2.gridwidth = 2;
        bottom.add(lblTotal, c2);
        c2.gridx = 4;
        c2.gridwidth = 1;
        bottom.add(btnFactura, c2);

        panelContenido.add(bottom, BorderLayout.SOUTH);
    }

    private void agregarAlCarrito() {
        Conexion.ProductoInfo p = (Conexion.ProductoInfo) comboProductos.getSelectedItem();
        if (p == null) {
            return;
        }
        int cantidad = (Integer) spinnerCantidad.getValue();
        carrito.add(new Conexion.ItemPedidoNuevo(p.idProducto, p.nombre, cantidad, p.precio));
        modeloCarrito.addRow(new Object[]{p.nombre, cantidad, String.format("Q%.2f", p.precio),
            String.format("Q%.2f", cantidad * p.precio)});
        actualizarTotal();
    }

    private void quitarDeCarrito(JTable tabla) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return;
        }
        carrito.remove(fila);
        modeloCarrito.removeRow(fila);
        actualizarTotal();
    }

    private void actualizarTotal() {
        double total = 0;
        for (Conexion.ItemPedidoNuevo item : carrito) {
            total += item.getSubtotal();
        }
        lblTotal.setText(String.format("Total: Q%.2f", total));
    }

    private void generarFactura() {
        if (carrito.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Agrega al menos un producto al pedido.",
                    "Pedido vacío", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String cliente = txtCliente.getText();
        String metodoPago = (String) comboMetodoPago.getSelectedItem();

        int idPedido = Conexion.registrarPedido(cliente, carrito, metodoPago);
        if (idPedido == -1) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo generar el pedido: no hay existencia suficiente de uno o más productos.",
                    "Sin stock", JOptionPane.ERROR_MESSAGE);
            return;
        }

        mostrarFactura(idPedido, cliente, metodoPago);

        carrito.clear();
        modeloCarrito.setRowCount(0);
        actualizarTotal();
        // refresca el combo de productos para reflejar el nuevo stock
        List<Conexion.ProductoInfo> productos = Conexion.listarProductos(true);
        comboProductos.removeAllItems();
        for (Conexion.ProductoInfo p : productos) {
            comboProductos.addItem(p);
        }
    }

    private void mostrarFactura(int idPedido, String cliente, String metodoPago) {
        StringBuilder sb = new StringBuilder();
        sb.append("========= WALDONALD'S =========\n");
        sb.append("Factura No.: ").append(idPedido).append("\n");
        sb.append("Fecha: ").append(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date())).append("\n");
        sb.append("Cliente: ").append(cliente).append("\n");
        sb.append("Método de pago: ").append(metodoPago).append("\n");
        sb.append("--------------------------------\n");
        double total = 0;
        for (Conexion.ItemPedidoNuevo item : carrito) {
            sb.append(String.format("%-22s x%-3d Q%8.2f%n", item.nombreProducto, item.cantidad, item.getSubtotal()));
            total += item.getSubtotal();
        }
        sb.append("--------------------------------\n");
        sb.append(String.format("TOTAL: Q%.2f%n", total));
        sb.append("================================");

        JTextArea area = new JTextArea(sb.toString());
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Factura generada",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
