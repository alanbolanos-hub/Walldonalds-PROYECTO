package GUI;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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
 * "Gestionar promociones": lista las promociones existentes, cuánto tiempo
 * les queda de vigencia, y permite crear nuevas o activar/desactivar.
 */
public class PanelGestionarPromociones extends PanelAdminBase {

    private DefaultTableModel modelo;
    private JTable tabla;

    public PanelGestionarPromociones() {
        super("Gestionar Promociones");
        construirContenido();
        cargarDatos();
    }

    private void construirContenido() {
        panelContenido.setLayout(new BorderLayout(10, 10));
        panelContenido.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // ---- Formulario para crear promoción ----
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Nueva promoción"));
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtNombre = new JTextField(14);
        JTextField txtCodigo = new JTextField(8);
        JComboBox<String> comboTipo = new JComboBox<>(new String[]{"Porcentaje", "Monto Fijo"});
        JTextField txtValor = new JTextField(6);
        JTextField txtInicio = new JTextField("2026-01-01", 10);
        JTextField txtFin = new JTextField("2026-12-31", 10);
        JSpinner spinnerMinimo = new JSpinner(new SpinnerNumberModel(5, 0, 1000, 1));

        JButton btnCrear = new JButton("Crear promoción");
        btnCrear.addActionListener(e -> crearPromocion(txtNombre, txtCodigo, comboTipo, txtValor,
                txtInicio, txtFin, spinnerMinimo));

        c.gridx = 0;
        c.gridy = 0;
        form.add(new JLabel("Nombre:"), c);
        c.gridx = 1;
        form.add(txtNombre, c);
        c.gridx = 2;
        form.add(new JLabel("Código:"), c);
        c.gridx = 3;
        form.add(txtCodigo, c);
        c.gridx = 4;
        form.add(new JLabel("Tipo:"), c);
        c.gridx = 5;
        form.add(comboTipo, c);

        c.gridx = 0;
        c.gridy = 1;
        form.add(new JLabel("Valor:"), c);
        c.gridx = 1;
        form.add(txtValor, c);
        c.gridx = 2;
        form.add(new JLabel("Inicio (AAAA-MM-DD):"), c);
        c.gridx = 3;
        form.add(txtInicio, c);
        c.gridx = 4;
        form.add(new JLabel("Fin (AAAA-MM-DD):"), c);
        c.gridx = 5;
        form.add(txtFin, c);

        c.gridx = 0;
        c.gridy = 2;
        form.add(new JLabel("Cant. mínima:"), c);
        c.gridx = 1;
        form.add(spinnerMinimo, c);
        c.gridx = 5;
        form.add(btnCrear, c);

        panelContenido.add(form, BorderLayout.NORTH);

        // ---- Tabla de promociones ----
        modelo = new DefaultTableModel(new Object[]{"ID", "Nombre", "Código", "Tipo", "Valor",
            "Vence", "Días restantes", "Activa"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tabla = new JTable(modelo);
        tabla.setRowHeight(26);
        panelContenido.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JButton btnToggle = new JButton("Activar / Desactivar seleccionada");
        btnToggle.setFont(new Font("Dialog", Font.BOLD, 13));
        btnToggle.addActionListener(e -> alternarEstado());
        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.add(btnToggle);
        panelContenido.add(wrapper, BorderLayout.SOUTH);
    }

    private void cargarDatos() {
        modelo.setRowCount(0);
        for (Conexion.PromocionInfo p : Conexion.listarPromociones()) {
            long dias = p.diasRestantes();
            String diasTexto = dias == Long.MIN_VALUE ? "N/D" : (dias < 0 ? "Expirada" : dias + " días");
            String valorTexto = p.tipoDescuento.equalsIgnoreCase("Porcentaje")
                    ? String.format("%.0f%%", p.valorDescuento) : String.format("Q%.2f", p.valorDescuento);
            modelo.addRow(new Object[]{p.idPromocion, p.nombre, p.codigo, p.tipoDescuento, valorTexto,
                p.fechaFin, diasTexto, p.activa ? "Sí" : "No"});
        }
    }

    private void crearPromocion(JTextField txtNombre, JTextField txtCodigo, JComboBox<String> comboTipo,
            JTextField txtValor, JTextField txtInicio, JTextField txtFin, JSpinner spinnerMinimo) {
        String nombre = txtNombre.getText().trim();
        String codigo = txtCodigo.getText().trim();
        if (nombre.isEmpty() || codigo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nombre y código son obligatorios.",
                    "Falta información", JOptionPane.WARNING_MESSAGE);
            return;
        }
        double valor;
        try {
            valor = Double.parseDouble(txtValor.getText().trim().replace(",", "."));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingresa un valor numérico.", "Valor inválido",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        boolean ok = Conexion.crearPromocion(nombre, "", codigo, (String) comboTipo.getSelectedItem(),
                valor, txtInicio.getText().trim(), txtFin.getText().trim(), (Integer) spinnerMinimo.getValue());
        if (ok) {
            txtNombre.setText("");
            txtCodigo.setText("");
            txtValor.setText("");
            cargarDatos();
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo crear la promoción (¿código repetido?).",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void alternarEstado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una promoción de la tabla.",
                    "Sin selección", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int idPromocion = (int) modelo.getValueAt(fila, 0);
        boolean estaActiva = "Sí".equals(modelo.getValueAt(fila, 7));
        if (Conexion.cambiarEstadoPromocion(idPromocion, !estaActiva)) {
            cargarDatos();
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo cambiar el estado.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
