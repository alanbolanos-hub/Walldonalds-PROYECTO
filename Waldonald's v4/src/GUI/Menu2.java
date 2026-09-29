package GUI;

import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.Timer;
import java.time.LocalTime;
import java.awt.Image;
import javax.swing.ImageIcon;

public class Menu2 extends javax.swing.JPanel {

    private javax.swing.JLabel labelFondo;

    public Menu2() {
        initComponents();

        ImageIcon iconoFondo = new ImageIcon(getClass().getResource("/Imagenes/Fondo.png"));
        labelFondo = new javax.swing.JLabel(iconoFondo);
        labelFondo.setBounds(0, 0, getWidth(), getHeight());
        add(labelFondo);
        setComponentZOrder(labelFondo, getComponentCount() - 1);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                labelFondo.setBounds(0, 0, getWidth(), getHeight());
            }
        });

        BotonesLabel();
    }

    private void abrirMenu() {
        Menu ventana = new Menu();

        ventana.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
        ventana.setVisible(true);

        // Obtener el JFrame que contiene este JPanel
        javax.swing.JFrame ventanaActual
                = (javax.swing.JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);

        // Cerrar la ventana actual
        if (ventanaActual != null) {
            ventanaActual.dispose();
        }
    }

    private void desayuno() {
        Desayunos ventana = new Desayunos();

        ventana.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
        ventana.setVisible(true);

        // Obtener el JFrame que contiene este JPanel
        javax.swing.JFrame ventanaActual
                = (javax.swing.JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);

        // Cerrar la ventana actual
        if (ventanaActual != null) {
            ventanaActual.dispose();
        }
    }
    
    private void Bebidas() {
        Bebidas ventana = new Bebidas();

        ventana.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
        ventana.setVisible(true);

        // Obtener el JFrame que contiene este JPanel
        javax.swing.JFrame ventanaActual
                = (javax.swing.JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);

        // Cerrar la ventana actual
        if (ventanaActual != null) {
            ventanaActual.dispose();
        }
    }
    
    private void AlmuerzoCena() {
        AlmuerzoCena ventana = new AlmuerzoCena();

        ventana.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
        ventana.setVisible(true);

        // Obtener el JFrame que contiene este JPanel
        javax.swing.JFrame ventanaActual
                = (javax.swing.JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);

        // Cerrar la ventana actual
        if (ventanaActual != null) {
            ventanaActual.dispose();
        }
    }
    private void Postre() {
        Postres ventana = new Postres();

        ventana.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
        ventana.setVisible(true);

        // Obtener el JFrame que contiene este JPanel
        javax.swing.JFrame ventanaActual
                = (javax.swing.JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);

        // Cerrar la ventana actual
        if (ventanaActual != null) {
            ventanaActual.dispose();
        }
    }
    private void CajitaWally() {
        CajitaWally ventana = new CajitaWally();

        ventana.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
        ventana.setVisible(true);

        // Obtener el JFrame que contiene este JPanel
        javax.swing.JFrame ventanaActual
                = (javax.swing.JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);

        // Cerrar la ventana actual
        if (ventanaActual != null) {
            ventanaActual.dispose();
        }
    }
    private void Cafes() {
        Cafes ventana = new Cafes();

        ventana.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
        ventana.setVisible(true);

        // Obtener el JFrame que contiene este JPanel
        javax.swing.JFrame ventanaActual
                = (javax.swing.JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);

        // Cerrar la ventana actual
        if (ventanaActual != null) {
            ventanaActual.dispose();
        }
    }
     private void Promociones() {
        Promociones ventana = new Promociones();

        ventana.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
        ventana.setVisible(true);

        // Obtener el JFrame que contiene este JPanel
        javax.swing.JFrame ventanaActual
                = (javax.swing.JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);

        // Cerrar la ventana actual
        if (ventanaActual != null) {
            ventanaActual.dispose();
        }
    }
     private void Antojos() {
        Antojos ventana = new Antojos();

        ventana.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
        ventana.setVisible(true);

        // Obtener el JFrame que contiene este JPanel
        javax.swing.JFrame ventanaActual
                = (javax.swing.JFrame) javax.swing.SwingUtilities.getWindowAncestor(this);

        // Cerrar la ventana actual
        if (ventanaActual != null) {
            ventanaActual.dispose();
        }
    }

    private void BotonesLabel() {
        jLabel1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        jLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                desayuno();
            }

            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel1.setOpaque(true);
                jLabel1.setBackground(new java.awt.Color(220, 220, 220)); // gris clarito al pasar el mouse
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel1.setOpaque(false);
            }
        });

        jLabel6.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        jLabel6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                Bebidas();
            }

            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel6.setOpaque(true);
                jLabel6.setBackground(new java.awt.Color(220, 220, 220)); // gris clarito al pasar el mouse
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel6.setOpaque(false);
            }
        });

        jLabel7.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        jLabel7.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                Cafes();
            }

            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel7.setOpaque(true);
                jLabel7.setBackground(new java.awt.Color(220, 220, 220)); // gris clarito al pasar el mouse
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel7.setOpaque(false);
            }
        });

        jLabel8.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        jLabel8.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                CajitaWally();
            }

            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel8.setOpaque(true);
                jLabel8.setBackground(new java.awt.Color(220, 220, 220)); // gris clarito al pasar el mouse
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel8.setOpaque(false);
            }
        });
        
        jLabel9.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        jLabel9.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                Promociones();
            }

            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel9.setOpaque(true);
                jLabel9.setBackground(new java.awt.Color(220, 220, 220)); // gris clarito al pasar el mouse
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel9.setOpaque(false);
            }
        });

        jLabel10.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        jLabel10.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                
                Postre();
                
            }

            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel10.setOpaque(true);
                jLabel10.setBackground(new java.awt.Color(220, 220, 220)); // gris clarito al pasar el mouse
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel10.setOpaque(false);
            }
        });

        jLabel11.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        jLabel11.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                Antojos();
            }

            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel11.setOpaque(true);
                jLabel11.setBackground(new java.awt.Color(220, 220, 220)); // gris clarito al pasar el mouse
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel11.setOpaque(false);
            }
        });

        jLabel12.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        jLabel12.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                AlmuerzoCena();
            }

            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel12.setOpaque(true);
                jLabel12.setBackground(new java.awt.Color(220, 220, 220)); // gris clarito al pasar el mouse
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel12.setOpaque(false);
            }
        });
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();

        jLabel3.setText("jLabel3");

        jLabel4.setText("jLabel4");

        jPanel1.setBackground(new java.awt.Color(0, 0, 0));

        jLabel5.setFont(new java.awt.Font("Segoe UI Semibold", 0, 36)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(209, 146, 51));
        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/Titulo.jpg"))); // NOI18N
        jLabel5.setText("Bienvenido al Menu Wall");

        jButton1.setBackground(new java.awt.Color(204, 0, 0));
        jButton1.setFont(new java.awt.Font("Cambria Math", 0, 18)); // NOI18N
        jButton1.setText("Regresar");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 1013, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(21, 21, 21))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel1.setFont(new java.awt.Font("Segoe UI Semibold", 0, 24)); // NOI18N
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/Desayunos.png"))); // NOI18N
        jLabel1.setText("Desayuno");
        jLabel1.setToolTipText("");

        jLabel12.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        jLabel12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/Almuerzo cena.png"))); // NOI18N
        jLabel12.setText("Almuerzo \n y Cena");
        jLabel12.setToolTipText("");

        jLabel10.setFont(new java.awt.Font("Segoe UI Semibold", 0, 24)); // NOI18N
        jLabel10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/Postres.png"))); // NOI18N
        jLabel10.setText("Postres");
        jLabel10.setToolTipText("");

        jLabel11.setFont(new java.awt.Font("Segoe UI Semibold", 0, 24)); // NOI18N
        jLabel11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/Antojos.png"))); // NOI18N
        jLabel11.setText("Antojos");
        jLabel11.setToolTipText("");

        jLabel6.setFont(new java.awt.Font("Segoe UI Semibold", 0, 24)); // NOI18N
        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/Bebidas.png"))); // NOI18N
        jLabel6.setText("Bebidas");
        jLabel6.setToolTipText("");

        jLabel7.setFont(new java.awt.Font("Segoe UI Semibold", 0, 24)); // NOI18N
        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/Wall cafe.png"))); // NOI18N
        jLabel7.setText("Wall Cafe");
        jLabel7.setToolTipText("");

        jLabel8.setFont(new java.awt.Font("Segoe UI Semibold", 0, 18)); // NOI18N
        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/Cajita Wally.png"))); // NOI18N
        jLabel8.setText("Cajita Wally");
        jLabel8.setToolTipText("");

        jLabel9.setFont(new java.awt.Font("Segoe UI Semibold", 0, 18)); // NOI18N
        jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/promociones.png"))); // NOI18N
        jLabel9.setText("Promociones");
        jLabel9.setToolTipText("");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(159, 159, 159)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(162, 162, 162)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(210, 210, 210)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(67, 67, 67)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel12)
                    .addComponent(jLabel10))
                .addGap(45, 45, 45)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jLabel6)
                    .addComponent(jLabel11))
                .addGap(63, 63, 63)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jLabel9))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        Menu menu = new Menu();
        menu.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH);
        menu.setVisible(true);

        // Cerrar la ventana que contiene este panel (Menu2)
        javax.swing.SwingUtilities.getWindowAncestor(this).dispose();
    }//GEN-LAST:event_jButton1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables

    void setExtendedState(int MAXIMIZED_BOTH) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
