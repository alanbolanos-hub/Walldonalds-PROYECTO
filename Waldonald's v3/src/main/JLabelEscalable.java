package main;

import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class JLabelEscalable extends JLabel {
    private Image imagen;

    @Override
    public void setIcon(javax.swing.Icon icon) {
        super.setIcon(icon);
        if (icon instanceof ImageIcon) {
            this.imagen = ((ImageIcon) icon).getImage();
        } else {
            this.imagen = null;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (imagen != null) {
            // Dibuja la imagen estirándose a todo el ancho y alto del Label
            g.drawImage(imagen, 0, 0, getWidth(), getHeight(), this);
        } else {
            super.paintComponent(g);
        }
    }
}