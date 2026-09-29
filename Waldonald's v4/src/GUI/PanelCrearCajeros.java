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
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import main.Conexion;

/**
 * "Crear cajeros": crea cuentas nuevas (administrador o cajero) en la
 * tabla Usuarios.
 */
public class PanelCrearCajeros extends PanelAdminBase {

    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JComboBox<String> comboRol;
    private Map<Integer, String> mapaRoles;
    private DefaultTableModel modelo;

    public PanelCrearCajeros() {
        super("Crear Cajeros");
        construirContenido();
        cargarUsuarios();
    }

    private void construirContenido() {
        panelContenido.setLayout(new BorderLayout(10, 10));
        panelContenido.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Nueva cuenta"));
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        mapaRoles = Conexion.listarRoles();
        comboRol = new JComboBox<>(mapaRoles.values().toArray(new String[0]));
        txtUsuario = new JTextField(14);
        txtPassword = new JPasswordField(14);

        JButton btnCrear = new JButton("Crear cuenta");
        btnCrear.setFont(new Font("Dialog", Font.BOLD, 13));
        btnCrear.addActionListener(e -> crearCuenta());

        c.gridx = 0;
        c.gridy = 0;
        form.add(new JLabel("Usuario:"), c);
        c.gridx = 1;
        form.add(txtUsuario, c);
        c.gridx = 2;
        form.add(new JLabel("Contraseña:"), c);
        c.gridx = 3;
        form.add(txtPassword, c);
        c.gridx = 4;
        form.add(new JLabel("Rol:"), c);
        c.gridx = 5;
        form.add(comboRol, c);
        c.gridx = 6;
        form.add(btnCrear, c);

        panelContenido.add(form, BorderLayout.NORTH);

        modelo = new DefaultTableModel(new Object[]{"ID", "Usuario", "Rol", "Activo"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(24);
        panelContenido.add(new JScrollPane(tabla), BorderLayout.CENTER);
    }

    private void cargarUsuarios() {
        modelo.setRowCount(0);
        for (Conexion.UsuarioInfo u : Conexion.listarUsuarios()) {
            modelo.addRow(new Object[]{u.idUsuario, u.usuario, u.rol, u.activo ? "Sí" : "No"});
        }
    }

    private void crearCuenta() {
        String usuario = txtUsuario.getText().trim();
        String password = new String(txtPassword.getPassword());
        if (usuario.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Usuario y contraseña son obligatorios.",
                    "Falta información", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String rolSeleccionado = (String) comboRol.getSelectedItem();
        int idRol = -1;
        for (Map.Entry<Integer, String> entry : mapaRoles.entrySet()) {
            if (entry.getValue().equals(rolSeleccionado)) {
                idRol = entry.getKey();
                break;
            }
        }
        if (idRol == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un rol.", "Falta información",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        boolean ok = Conexion.crearCajero(usuario, password, idRol);
        if (ok) {
            txtUsuario.setText("");
            txtPassword.setText("");
            cargarUsuarios();
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo crear la cuenta (¿el usuario ya existe?).",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
