package main;

import GUI.Entrada;

public class Main {

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            Entrada entrada = new Entrada();
            entrada.setVisible(true);
        });
    }
    
}