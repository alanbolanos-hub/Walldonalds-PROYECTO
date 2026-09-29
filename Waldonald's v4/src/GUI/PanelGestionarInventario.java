package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import main.Conexion;

/**
 * "Gestionar inventario": muestra cuánto hay de cada producto, ordenado del
 * que tiene MENOS existencia al que tiene MÁS (para detectar rápido lo que
 * se está por agotar).
 */
public class PanelGestionarInventario extends PanelAdminBase {

    private DefaultTableModel modelo;

    public PanelGestionarInventario() {
        super("Gestionar Inventario");
        construirContenido();
        cargarDatos();
    }

    private void construirContenido() {
        panelContenido.setLayout(new BorderLayout(10, 10));
        panelContenido.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titulo = new JLabel("Existencias ordenadas de MENOR a MAYOR cantidad:");
        titulo.setFont(new Font("Dialog", Font.BOLD, 14));
        panelContenido.add(titulo, BorderLayout.NORTH);

        modelo = new DefaultTableModel(
                new Object[]{"Categoría", "Producto", "Existencia", "Mínimo recomendado", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(26);

        // Resalta en rojo las filas con existencia por debajo del mínimo.
        DefaultTableCellRenderer resaltador = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor, boolean sel,
                    boolean foco, int fila, int col) {
                Component comp = super.getTableCellRendererComponent(t, valor, sel, foco, fila, col);
                int stock = (int) t.getModel().getValueAt(t.convertRowIndexToModel(fila), 2);
                int minimo = (int) t.getModel().getValueAt(t.convertRowIndexToModel(fila), 3);
                if (!sel) {
                    comp.setBackground(stock < minimo ? new Color(250, 210, 205) : Color.WHITE);
                }
                return comp;
            }
        };
        for (int i = 0; i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setCellRenderer(resaltador);
        }

        panelContenido.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JButton btnRefrescar = new JButton("Actualizar");
        btnRefrescar.addActionListener(e -> cargarDatos());
        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.add(btnRefrescar);
        panelContenido.add(wrapper, BorderLayout.SOUTH);
    }

    private void cargarDatos() {
        modelo.setRowCount(0);
        for (Conexion.InventarioInfo inv : Conexion.listarInventarioOrdenado()) {
            String estado = inv.stockActual < inv.stockMinimo ? "Bajo" : "OK";
            modelo.addRow(new Object[]{inv.categoria, inv.nombreProducto, inv.stockActual, inv.stockMinimo, estado});
        }
    }
}
