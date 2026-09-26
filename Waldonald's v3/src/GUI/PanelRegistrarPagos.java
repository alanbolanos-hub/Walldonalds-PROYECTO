package GUI;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import main.Conexion;

/**
 * "Registrar pagos": lleva el registro de lo que se ha cobrado sobre los
 * pedidos generados en "Registrar pedidos". Permite elegir un pedido sin
 * pago registrado, indicar método de pago y monto, y lo guarda en Pagos.
 */
public class PanelRegistrarPagos extends PanelAdminBase {

    private JComboBox<Conexion.PedidoInfo> comboPedidos;
    private JComboBox<String> comboMetodo;
    private JTextField txtMonto;
    private DefaultTableModel modeloPagos;

    public PanelRegistrarPagos() {
        super("Registrar Pagos");
        construirContenido();
        cargarPagos();
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
        cargarPedidosPendientes();

        comboMetodo = new JComboBox<>(new String[]{"Efectivo", "Tarjeta", "Transferencia"});
        txtMonto = new JTextField(10);

        JButton btnRegistrar = new JButton("Registrar pago");
        btnRegistrar.setFont(new Font("Dialog", Font.BOLD, 13));
        btnRegistrar.addActionListener(e -> registrarPago());

        c.gridx = 0;
        c.gridy = 0;
        top.add(new JLabel("Pedido pendiente:"), c);
        c.gridx = 1;
        c.weightx = 1;
        top.add(comboPedidos, c);
        c.gridx = 2;
        c.weightx = 0;
        top.add(new JLabel("Método:"), c);
        c.gridx = 3;
        top.add(comboMetodo, c);
        c.gridx = 4;
        top.add(new JLabel("Monto (Q):"), c);
        c.gridx = 5;
        top.add(txtMonto, c);
        c.gridx = 6;
        top.add(btnRegistrar, c);

        panelContenido.add(top, BorderLayout.NORTH);

        modeloPagos = new DefaultTableModel(
                new Object[]{"ID Pago", "ID Pedido", "Método", "Monto", "Fecha", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        JTable tabla = new JTable(modeloPagos);
        tabla.setRowHeight(24);
        JLabel lblHistorial = new JLabel("  Historial de pagos registrados:");
        lblHistorial.setFont(new Font("Dialog", Font.BOLD, 14));

        JPanel centro = new JPanel(new BorderLayout());
        centro.setOpaque(false);
        centro.add(lblHistorial, BorderLayout.NORTH);
        centro.add(new JScrollPane(tabla), BorderLayout.CENTER);
        panelContenido.add(centro, BorderLayout.CENTER);
    }

    private void cargarPedidosPendientes() {
        comboPedidos.removeAllItems();
        List<Conexion.PedidoInfo> pendientes = Conexion.listarPedidosSinPago();
        for (Conexion.PedidoInfo p : pendientes) {
            comboPedidos.addItem(p);
        }
        comboPedidos.setRenderer((lista, valor, idx, sel, foco) -> {
            JLabel l = new JLabel(valor == null ? "(no hay pedidos pendientes)"
                    : "Pedido #" + valor.idPedido + " - " + valor.cliente + " - Q" + String.format("%.2f", valor.total));
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
    }

    private void cargarPagos() {
        modeloPagos.setRowCount(0);
        for (Conexion.PagoInfo p : Conexion.listarPagos()) {
            modeloPagos.addRow(new Object[]{p.idPago, p.idPedido, p.metodoPago,
                String.format("Q%.2f", p.monto), p.fecha, p.estado});
        }
    }

    private void registrarPago() {
        Conexion.PedidoInfo pedido = (Conexion.PedidoInfo) comboPedidos.getSelectedItem();
        if (pedido == null) {
            JOptionPane.showMessageDialog(this, "No hay pedidos pendientes de pago.",
                    "Sin pedidos", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        double monto;
        try {
            monto = Double.parseDouble(txtMonto.getText().trim().replace(",", "."));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingresa un monto válido.", "Monto inválido",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String metodo = (String) comboMetodo.getSelectedItem();
        boolean ok = Conexion.registrarPago(pedido.idPedido, metodo, monto);
        if (ok) {
            txtMonto.setText("");
            cargarPedidosPendientes();
            cargarPagos();
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo registrar el pago.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
