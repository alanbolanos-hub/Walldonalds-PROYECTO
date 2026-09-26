package GUI;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

import main.Conexion;

/**
 * "Crear productos": funciona como el almacén. Muestra todos los productos
 * de la base de datos con su existencia, con un campo para sumar o restar
 * cantidad, y además permite crear productos nuevos.
 */
public class PanelCrearProductos extends PanelAdminBase {

    private DefaultTableModel modeloAlmacen;
    private JTable tablaAlmacen;
    private JComboBox<String> comboCategoriaNueva;
    private Map<Integer, String> mapaCategorias;

    public PanelCrearProductos() {
        super("Crear Productos (Almacén)");
        construirContenido();
        cargarAlmacen();
    }

    private void construirContenido() {
        panelContenido.setLayout(new BorderLayout(10, 10));
        panelContenido.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // ---- Formulario para crear un producto nuevo ----
        JPanel formNuevo = new JPanel(new GridBagLayout());
        formNuevo.setBorder(BorderFactory.createTitledBorder("Nuevo producto"));
        formNuevo.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        mapaCategorias = new java.util.LinkedHashMap<>();
        for (Conexion.CategoriaInfo cat : Conexion.listarCategorias()) {
            mapaCategorias.put(cat.idCategoria, cat.nombre);
        }
        comboCategoriaNueva = new JComboBox<>(mapaCategorias.values().toArray(new String[0]));

        JTextField txtNombre = new JTextField(18);
        JTextField txtDescripcion = new JTextField(18);
        JTextField txtPrecio = new JTextField(8);
        JSpinner spinnerStockInicial = new JSpinner(new SpinnerNumberModel(0, 0, 100000, 1));

        JButton btnCrear = new JButton("Crear producto");
        btnCrear.setFont(new Font("Dialog", Font.BOLD, 13));
        btnCrear.addActionListener(e -> crearProducto(txtNombre, txtDescripcion, txtPrecio, spinnerStockInicial));

        c.gridx = 0;
        c.gridy = 0;
        formNuevo.add(new JLabel("Categoría:"), c);
        c.gridx = 1;
        formNuevo.add(comboCategoriaNueva, c);
        c.gridx = 2;
        formNuevo.add(new JLabel("Nombre:"), c);
        c.gridx = 3;
        formNuevo.add(txtNombre, c);

        c.gridx = 0;
        c.gridy = 1;
        formNuevo.add(new JLabel("Descripción:"), c);
        c.gridx = 1;
        formNuevo.add(txtDescripcion, c);
        c.gridx = 2;
        formNuevo.add(new JLabel("Precio (Q):"), c);
        c.gridx = 3;
        formNuevo.add(txtPrecio, c);

        c.gridx = 0;
        c.gridy = 2;
        formNuevo.add(new JLabel("Existencia inicial:"), c);
        c.gridx = 1;
        formNuevo.add(spinnerStockInicial, c);
        c.gridx = 3;
        formNuevo.add(btnCrear, c);

        panelContenido.add(formNuevo, BorderLayout.NORTH);

        // ---- Tabla del almacén con ajuste de existencia ----
        modeloAlmacen = new DefaultTableModel(
                new Object[]{"ID", "Categoría", "Producto", "Precio (Q)", "Existencia"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tablaAlmacen = new JTable(modeloAlmacen);
        tablaAlmacen.setRowHeight(26);
        panelContenido.add(new JScrollPane(tablaAlmacen), BorderLayout.CENTER);

        JPanel ajuste = new JPanel(new GridBagLayout());
        ajuste.setOpaque(false);
        GridBagConstraints c2 = new GridBagConstraints();
        c2.insets = new Insets(4, 6, 4, 6);

        JSpinner spinnerAjuste = new JSpinner(new SpinnerNumberModel(1, 1, 100000, 1));
        JButton btnSumar = new JButton("Sumar existencia (+)");
        JButton btnRestar = new JButton("Restar existencia (−)");
        btnSumar.addActionListener(e -> ajustarStock((Integer) spinnerAjuste.getValue()));
        btnRestar.addActionListener(e -> ajustarStock(-(Integer) spinnerAjuste.getValue()));

        c2.gridx = 0;
        ajuste.add(new JLabel("Cantidad a ajustar:"), c2);
        c2.gridx = 1;
        ajuste.add(spinnerAjuste, c2);
        c2.gridx = 2;
        ajuste.add(btnSumar, c2);
        c2.gridx = 3;
        ajuste.add(btnRestar, c2);

        panelContenido.add(ajuste, BorderLayout.SOUTH);
    }

    private void cargarAlmacen() {
        modeloAlmacen.setRowCount(0);
        for (Conexion.ProductoInfo p : Conexion.listarProductos(true)) {
            modeloAlmacen.addRow(new Object[]{p.idProducto, p.categoria, p.nombre,
                String.format("Q%.2f", p.precio), p.stock});
        }
    }

    private void crearProducto(JTextField txtNombre, JTextField txtDescripcion, JTextField txtPrecio,
            JSpinner spinnerStockInicial) {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre del producto es obligatorio.",
                    "Falta información", JOptionPane.WARNING_MESSAGE);
            return;
        }
        double precio;
        try {
            precio = Double.parseDouble(txtPrecio.getText().trim().replace(",", "."));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingresa un precio válido.", "Precio inválido",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String nombreCategoria = (String) comboCategoriaNueva.getSelectedItem();
        int idCategoria = -1;
        for (Map.Entry<Integer, String> entry : mapaCategorias.entrySet()) {
            if (entry.getValue().equals(nombreCategoria)) {
                idCategoria = entry.getKey();
                break;
            }
        }
        if (idCategoria == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona una categoría.", "Falta información",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int stockInicial = (Integer) spinnerStockInicial.getValue();
        boolean ok = Conexion.crearProducto(idCategoria, nombre, txtDescripcion.getText().trim(), precio, stockInicial);
        if (ok) {
            txtNombre.setText("");
            txtDescripcion.setText("");
            txtPrecio.setText("");
            spinnerStockInicial.setValue(0);
            cargarAlmacen();
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo crear el producto.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ajustarStock(int delta) {
        int fila = tablaAlmacen.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto de la tabla primero.",
                    "Sin selección", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int idProducto = (int) modeloAlmacen.getValueAt(fila, 0);
        if (Conexion.ajustarStock(idProducto, delta)) {
            cargarAlmacen();
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo ajustar la existencia.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
