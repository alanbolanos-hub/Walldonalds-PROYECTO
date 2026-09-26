package GUI;

import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import main.Conexion;

/**
 * "Eliminar productos": permite eliminar un producto del menú (baja lógica,
 * para no romper el historial de pedidos que ya lo referencian).
 */
public class PanelEliminarProductos extends PanelAdminBase {

    private DefaultTableModel modelo;
    private JTable tabla;

    public PanelEliminarProductos() {
        super("Eliminar Productos");
        construirContenido();
        cargarDatos();
    }

    private void construirContenido() {
        panelContenido.setLayout(new BorderLayout(10, 10));
        panelContenido.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel aviso = new JLabel("Selecciona un producto y presiona \"Eliminar producto\".");
        aviso.setFont(new Font("Dialog", Font.PLAIN, 13));
        panelContenido.add(aviso, BorderLayout.NORTH);

        modelo = new DefaultTableModel(new Object[]{"ID", "Categoría", "Producto", "Precio (Q)", "Existencia"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tabla = new JTable(modelo);
        tabla.setRowHeight(26);
        panelContenido.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JButton btnEliminar = new JButton("Eliminar producto seleccionado");
        btnEliminar.setFont(new Font("Dialog", Font.BOLD, 13));
        btnEliminar.setBackground(new java.awt.Color(160, 38, 35));
        btnEliminar.setForeground(java.awt.Color.WHITE);
        btnEliminar.addActionListener(e -> eliminarSeleccionado());

        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.add(btnEliminar);
        panelContenido.add(wrapper, BorderLayout.SOUTH);
    }

    private void cargarDatos() {
        modelo.setRowCount(0);
        for (Conexion.ProductoInfo p : Conexion.listarProductos(true)) {
            modelo.addRow(new Object[]{p.idProducto, p.categoria, p.nombre,
                String.format("Q%.2f", p.precio), p.stock});
        }
    }

    private void eliminarSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto de la tabla.",
                    "Sin selección", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int idProducto = (int) modelo.getValueAt(fila, 0);
        String nombre = (String) modelo.getValueAt(fila, 2);

        int confirmar = JOptionPane.showConfirmDialog(this,
                "¿Estás seguro de eliminar \"" + nombre + "\"? Ya no aparecerá en el menú.",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirmar != JOptionPane.YES_OPTION) {
            return;
        }
        if (Conexion.eliminarProducto(idProducto)) {
            cargarDatos();
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo eliminar el producto.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
