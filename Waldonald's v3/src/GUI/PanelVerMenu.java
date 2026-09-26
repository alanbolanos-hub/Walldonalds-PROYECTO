package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import main.Conexion;

/**
 * "Ver menú": tabla de solo lectura con todos los productos disponibles
 * de la base de datos (categoría, nombre, precio y existencia).
 */
public class PanelVerMenu extends PanelAdminBase {

    private DefaultTableModel modelo;
    private TableRowSorter<DefaultTableModel> ordenador;

    public PanelVerMenu() {
        super("Ver Menú");
        construirContenido();
        cargarDatos();
    }

    private void construirContenido() {
        panelContenido.setLayout(new BorderLayout(10, 10));
        panelContenido.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel top = new JPanel(new BorderLayout(10, 0));
        top.setOpaque(false);
        JLabel lblBuscar = new JLabel("Buscar:");
        lblBuscar.setFont(new Font("Dialog", Font.BOLD, 14));
        JTextField txtBuscar = new JTextField();
        top.add(lblBuscar, BorderLayout.WEST);
        top.add(txtBuscar, BorderLayout.CENTER);
        panelContenido.add(top, BorderLayout.NORTH);

        modelo = new DefaultTableModel(
                new Object[]{"ID", "Categoría", "Producto", "Precio (Q)", "Existencia"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(26);
        ordenador = new TableRowSorter<>(modelo);
        tabla.setRowSorter(ordenador);

        txtBuscar.addCaretListener(e -> {
            String texto = txtBuscar.getText();
            if (texto == null || texto.isEmpty()) {
                ordenador.setRowFilter(null);
            } else {
                ordenador.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(texto)));
            }
        });

        panelContenido.add(new JScrollPane(tabla), BorderLayout.CENTER);
    }

    private void cargarDatos() {
        modelo.setRowCount(0);
        List<Conexion.ProductoInfo> productos = Conexion.listarProductos(true);
        for (Conexion.ProductoInfo p : productos) {
            modelo.addRow(new Object[]{
                p.idProducto, p.categoria, p.nombre,
                String.format("Q%.2f", p.precio), p.stock
            });
        }
    }
}
