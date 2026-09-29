package GUI;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
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
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

import main.Conexion;

/**
 * "Modificar pedidos": elige un pedido ya realizado y permite cambiar los
 * productos/cantidades que contiene. Al guardar, se repone el inventario
 * de lo anterior, se descuenta el de lo nuevo y se recalcula el total.
 */
public class PanelModificarPedidos extends PanelAdminBase {

    private JComboBox<Conexion.PedidoInfo> comboPedidos;
    private JComboBox<Conexion.ProductoInfo> comboProductos;
    private JSpinner spinnerCantidad;
    private DefaultTableModel modeloItems;
    private JLabel lblTotal;

    private final List<Conexion.ItemPedidoNuevo> itemsActuales = new ArrayList<>();
    private int idPedidoSeleccionado = -1;

    public PanelModificarPedidos() {
        super("Modificar Pedidos");
        construirContenido();
    }

    private void construirContenido() {
        panelContenido.setLayout(new BorderLayout(10, 10));
        panelContenido.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel top = new JPanel(new GridBagLayout());
        top.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        comboPedidos = new JComboBox<>();
        for (Conexion.PedidoInfo p : Conexion.listarPedidos()) {
            comboPedidos.addItem(p);
        }
        comboPedidos.setRenderer((lista, valor, idx, sel, foco) -> {
            JLabel l = new JLabel(valor == null ? "" : "Pedido #" + valor.idPedido + " - " + valor.cliente
                    + " - Q" + String.format("%.2f", valor.total));
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
        comboPedidos.addActionListener(e -> cargarPedidoSeleccionado());

        c.gridx = 0;
        c.gridy = 0;
        top.add(new JLabel("Pedido a modificar:"), c);
        c.gridx = 1;
        c.weightx = 1;
        top.add(comboPedidos, c);

        panelContenido.add(top, BorderLayout.NORTH);

        modeloItems = new DefaultTableModel(new Object[]{"Producto", "Cantidad", "Precio unit.", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        JTable tabla = new JTable(modeloItems);
        tabla.setRowHeight(24);
        panelContenido.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new GridBagLayout());
        bottom.setOpaque(false);
        GridBagConstraints c2 = new GridBagConstraints();
        c2.insets = new Insets(4, 6, 4, 6);
        c2.fill = GridBagConstraints.HORIZONTAL;

        List<Conexion.ProductoInfo> productos = Conexion.listarProductos(true);
        comboProductos = new JComboBox<>(productos.toArray(new Conexion.ProductoInfo[0]));
        comboProductos.setRenderer((lista, valor, idx, sel, foco) -> {
            JLabel l = new JLabel(valor == null ? "" : valor.nombre + " - Q" + String.format("%.2f", valor.precio));
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

        JButton btnAgregar = new JButton("Agregar producto");
        btnAgregar.addActionListener(e -> agregarProducto());
        JButton btnQuitar = new JButton("Quitar seleccionado");
        btnQuitar.addActionListener(e -> quitarProducto(tabla));
        JButton btnGuardar = new JButton("Guardar cambios");
        btnGuardar.setFont(new Font("Dialog", Font.BOLD, 13));
        btnGuardar.addActionListener(e -> guardarCambios());

        lblTotal = new JLabel("Total: Q0.00");
        lblTotal.setFont(new Font("Dialog", Font.BOLD, 15));

        c2.gridx = 0;
        c2.gridy = 0;
        bottom.add(new JLabel("Producto:"), c2);
        c2.gridx = 1;
        c2.weightx = 1;
        bottom.add(comboProductos, c2);
        c2.gridx = 2;
        c2.weightx = 0;
        bottom.add(new JLabel("Cantidad:"), c2);
        c2.gridx = 3;
        bottom.add(spinnerCantidad, c2);
        c2.gridx = 4;
        bottom.add(btnAgregar, c2);
        c2.gridx = 5;
        bottom.add(btnQuitar, c2);

        c2.gridx = 0;
        c2.gridy = 1;
        c2.gridwidth = 2;
        bottom.add(lblTotal, c2);
        c2.gridx = 5;
        c2.gridwidth = 1;
        bottom.add(btnGuardar, c2);

        panelContenido.add(bottom, BorderLayout.SOUTH);
    }

    private void cargarPedidoSeleccionado() {
        Conexion.PedidoInfo pedido = (Conexion.PedidoInfo) comboPedidos.getSelectedItem();
        itemsActuales.clear();
        modeloItems.setRowCount(0);
        if (pedido == null) {
            idPedidoSeleccionado = -1;
            actualizarTotal();
            return;
        }
        idPedidoSeleccionado = pedido.idPedido;
        for (Conexion.DetallePedidoInfo d : Conexion.listarDetallePedido(pedido.idPedido)) {
            itemsActuales.add(new Conexion.ItemPedidoNuevo(d.idProducto, d.nombreProducto, d.cantidad, d.precioUnitario));
            modeloItems.addRow(new Object[]{d.nombreProducto, d.cantidad,
                String.format("Q%.2f", d.precioUnitario), String.format("Q%.2f", d.subtotal)});
        }
        actualizarTotal();
    }

    private void agregarProducto() {
        Conexion.ProductoInfo p = (Conexion.ProductoInfo) comboProductos.getSelectedItem();
        if (p == null || idPedidoSeleccionado == -1) {
            return;
        }
        int cantidad = (Integer) spinnerCantidad.getValue();
        itemsActuales.add(new Conexion.ItemPedidoNuevo(p.idProducto, p.nombre, cantidad, p.precio));
        modeloItems.addRow(new Object[]{p.nombre, cantidad, String.format("Q%.2f", p.precio),
            String.format("Q%.2f", cantidad * p.precio)});
        actualizarTotal();
    }

    private void quitarProducto(JTable tabla) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return;
        }
        itemsActuales.remove(fila);
        modeloItems.removeRow(fila);
        actualizarTotal();
    }

    private void actualizarTotal() {
        double total = 0;
        for (Conexion.ItemPedidoNuevo item : itemsActuales) {
            total += item.getSubtotal();
        }
        lblTotal.setText(String.format("Total: Q%.2f", total));
    }

    private void guardarCambios() {
        if (idPedidoSeleccionado == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un pedido primero.", "Sin selección",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (itemsActuales.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El pedido debe tener al menos un producto.",
                    "Pedido vacío", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirmar = JOptionPane.showConfirmDialog(this,
                "¿Guardar los cambios de este pedido? Esto ajustará el inventario.",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirmar != JOptionPane.YES_OPTION) {
            return;
        }
        boolean ok = Conexion.modificarPedido(idPedidoSeleccionado, itemsActuales);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Pedido actualizado correctamente.", "Listo",
                    JOptionPane.INFORMATION_MESSAGE);
            // refresca la lista de pedidos y productos (precios/stock pudieron cambiar)
            Conexion.PedidoInfo seleccionAnterior = null;
            comboPedidos.removeAllItems();
            for (Conexion.PedidoInfo p : Conexion.listarPedidos()) {
                comboPedidos.addItem(p);
                if (p.idPedido == idPedidoSeleccionado) {
                    seleccionAnterior = p;
                }
            }
            if (seleccionAnterior != null) {
                comboPedidos.setSelectedItem(seleccionAnterior);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "No se pudo guardar: no hay existencia suficiente de uno o más productos.",
                    "Sin stock", JOptionPane.ERROR_MESSAGE);
        }
    }
}
