package GUI;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import main.Conexion;

/**
 * "Modificar productos": permite cambiar el nombre, la descripción, el
 * precio o la imagen de un producto ya existente.
 */
public class PanelModificarProductos extends PanelAdminBase {

    private JComboBox<Conexion.ProductoInfo> comboProductos;
    private JTextField txtNombre;
    private JTextField txtDescripcion;
    private JTextField txtPrecio;
    private JTextField txtImagen;
    private DefaultTableModel modeloTabla;

    public PanelModificarProductos() {
        super("Modificar Productos");
        construirContenido();
    }

    private void construirContenido() {
        panelContenido.setLayout(new BorderLayout(10, 10));
        panelContenido.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        List<Conexion.ProductoInfo> productos = Conexion.listarProductos(true);
        comboProductos = new JComboBox<>(productos.toArray(new Conexion.ProductoInfo[0]));
        comboProductos.setRenderer((lista, valor, idx, sel, foco) -> {
            JLabel l = new JLabel(valor == null ? "" : valor.nombre + " (" + valor.categoria + ")");
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
        comboProductos.addActionListener(e -> cargarSeleccion());

        txtNombre = new JTextField(18);
        txtDescripcion = new JTextField(18);
        txtPrecio = new JTextField(8);
        txtImagen = new JTextField(18);
        txtImagen.setEditable(false);

        JButton btnImagen = new JButton("Seleccionar imagen...");
        btnImagen.addActionListener(e -> elegirImagen());

        JButton btnGuardar = new JButton("Guardar cambios");
        btnGuardar.setFont(new Font("Dialog", Font.BOLD, 13));
        btnGuardar.addActionListener(e -> guardarCambios());

        c.gridx = 0;
        c.gridy = 0;
        form.add(new JLabel("Producto:"), c);
        c.gridx = 1;
        c.gridwidth = 3;
        c.weightx = 1;
        form.add(comboProductos, c);
        c.gridwidth = 1;
        c.weightx = 0;

        c.gridx = 0;
        c.gridy = 1;
        form.add(new JLabel("Nombre:"), c);
        c.gridx = 1;
        form.add(txtNombre, c);
        c.gridx = 2;
        form.add(new JLabel("Precio (Q):"), c);
        c.gridx = 3;
        form.add(txtPrecio, c);

        c.gridx = 0;
        c.gridy = 2;
        form.add(new JLabel("Descripción:"), c);
        c.gridx = 1;
        c.gridwidth = 3;
        form.add(txtDescripcion, c);
        c.gridwidth = 1;

        c.gridx = 0;
        c.gridy = 3;
        form.add(new JLabel("Imagen:"), c);
        c.gridx = 1;
        form.add(txtImagen, c);
        c.gridx = 2;
        form.add(btnImagen, c);
        c.gridx = 3;
        form.add(btnGuardar, c);

        panelContenido.add(form, BorderLayout.NORTH);

        modeloTabla = new DefaultTableModel(
                new Object[]{"ID", "Categoría", "Producto", "Precio (Q)", "Imagen"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        JTable tabla = new JTable(modeloTabla);
        tabla.setRowHeight(24);
        panelContenido.add(new JScrollPane(tabla), BorderLayout.CENTER);

        cargarTabla();
        cargarSeleccion();
    }

    private void cargarTabla() {
        modeloTabla.setRowCount(0);
        for (Conexion.ProductoInfo p : Conexion.listarProductos(true)) {
            modeloTabla.addRow(new Object[]{p.idProducto, p.categoria, p.nombre,
                String.format("Q%.2f", p.precio), p.imagen == null ? "(sin imagen)" : p.imagen});
        }
    }

    private void cargarSeleccion() {
        Conexion.ProductoInfo p = (Conexion.ProductoInfo) comboProductos.getSelectedItem();
        if (p == null) {
            return;
        }
        txtNombre.setText(p.nombre);
        txtDescripcion.setText(p.descripcion == null ? "" : p.descripcion);
        txtPrecio.setText(String.format("%.2f", p.precio));
        txtImagen.setText(p.imagen == null ? "" : p.imagen);
    }

    private void elegirImagen() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Imágenes (png, jpg, jpeg, gif)", "png", "jpg", "jpeg", "gif"));
        int resultado = chooser.showOpenDialog(this);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            File archivo = chooser.getSelectedFile();
            txtImagen.setText(archivo.getAbsolutePath());
        }
    }

    private void guardarCambios() {
        Conexion.ProductoInfo p = (Conexion.ProductoInfo) comboProductos.getSelectedItem();
        if (p == null) {
            return;
        }
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre no puede quedar vacío.",
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
        boolean ok = Conexion.actualizarProducto(p.idProducto, nombre, txtDescripcion.getText().trim(),
                precio, txtImagen.getText().trim());
        if (ok) {
            JOptionPane.showMessageDialog(this, "Producto actualizado.", "Listo",
                    JOptionPane.INFORMATION_MESSAGE);
            List<Conexion.ProductoInfo> productos = Conexion.listarProductos(true);
            comboProductos.removeAllItems();
            for (Conexion.ProductoInfo np : productos) {
                comboProductos.addItem(np);
            }
            cargarTabla();
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo actualizar el producto.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
