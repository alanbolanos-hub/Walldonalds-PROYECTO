package GUI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import main.Conexion;

/**
 * "Consultar pedidos": muestra el historial completo de pedidos realizados.
 * Al seleccionar uno, se ve su detalle (productos, cantidades y subtotales).
 */
public class PanelConsultarPedidos extends PanelAdminBase {

    private DefaultTableModel modeloPedidos;
    private DefaultTableModel modeloDetalle;

    public PanelConsultarPedidos() {
        super("Consultar Pedidos");
        construirContenido();
        cargarPedidos();
    }

    private void construirContenido() {
        panelContenido.setLayout(new BorderLayout(10, 10));
        panelContenido.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        modeloPedidos = new DefaultTableModel(
                new Object[]{"ID", "Cliente", "Fecha", "Total", "Estado", "Pago", "Pagado"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        JTable tablaPedidos = new JTable(modeloPedidos);
        tablaPedidos.setRowHeight(24);
        tablaPedidos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        modeloDetalle = new DefaultTableModel(
                new Object[]{"Producto", "Cantidad", "Precio unit.", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        JTable tablaDetalle = new JTable(modeloDetalle);
        tablaDetalle.setRowHeight(24);

        ListSelectionListener seleccion = e -> {
            if (!e.getValueIsAdjusting() && tablaPedidos.getSelectedRow() >= 0) {
                int idPedido = (int) modeloPedidos.getValueAt(tablaPedidos.getSelectedRow(), 0);
                cargarDetalle(idPedido);
            }
        };
        tablaPedidos.getSelectionModel().addListSelectionListener(seleccion);

        JPanel arriba = new JPanel(new BorderLayout());
        arriba.setOpaque(false);
        arriba.add(new JLabel("Historial de pedidos:"), BorderLayout.NORTH);
        arriba.add(new JScrollPane(tablaPedidos), BorderLayout.CENTER);

        JPanel abajo = new JPanel(new BorderLayout());
        abajo.setOpaque(false);
        JLabel lblDetalle = new JLabel("Detalle del pedido seleccionado:");
        lblDetalle.setFont(new Font("Dialog", Font.BOLD, 13));
        abajo.add(lblDetalle, BorderLayout.NORTH);
        abajo.add(new JScrollPane(tablaDetalle), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, arriba, abajo);
        split.setResizeWeight(0.6);
        split.setDividerSize(6);
        split.setPreferredSize(new Dimension(10, 10));

        panelContenido.add(split, BorderLayout.CENTER);
    }

    private void cargarPedidos() {
        modeloPedidos.setRowCount(0);
        for (Conexion.PedidoInfo p : Conexion.listarPedidos()) {
            modeloPedidos.addRow(new Object[]{p.idPedido, p.cliente, p.fecha,
                String.format("Q%.2f", p.total), p.estado, p.metodoPago, p.pagado ? "Sí" : "No"});
        }
    }

    private void cargarDetalle(int idPedido) {
        modeloDetalle.setRowCount(0);
        for (Conexion.DetallePedidoInfo d : Conexion.listarDetallePedido(idPedido)) {
            modeloDetalle.addRow(new Object[]{d.nombreProducto, d.cantidad,
                String.format("Q%.2f", d.precioUnitario), String.format("Q%.2f", d.subtotal)});
        }
    }
}
