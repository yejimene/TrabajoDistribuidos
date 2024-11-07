import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;



public class ReservaAsientosCine {
    private JFrame principal= new JFrame();
    private JComboBox<String> comboPeliculas;
    private JComboBox<String> comboHoras;
    private JComboBox<String> comboAsientos;

    public ReservaAsientosCine() {
        principal.setTitle("Reserva Asientos Cine ");
        principal. setSize(400, 200);
        principal.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        principal.setLocationRelativeTo(null);

        // Panel de selección de película y hora
        JPanel panelSeleccion = new JPanel();
        panelSeleccion.setLayout(new GridLayout(4, 2, 10, 10));
        JLabel labelPelicula = new JLabel("Seleccione la película:");
        String[] peliculas = {"Pelicula 1", "Pelicula 2", "Pelicula 3"};
        comboPeliculas = new JComboBox<>(peliculas);
        JLabel labelHora = new JLabel("Seleccione la hora:");
        String[] horas = {"14:00", "17:00", "20:00"};
        comboHoras = new JComboBox<>(horas);
        JLabel labelAsientos = new JLabel("Seleccione la cantidad de asientos:");
        String[] cantidadAsientos = {"1", "2", "3"};
        comboAsientos= new JComboBox<>(cantidadAsientos);
        JButton btnConfirmarSeleccion = new JButton("Confirmar");
        btnConfirmarSeleccion.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mostrarSala();
            }
        });

        // Componentes del panel de selección
        panelSeleccion.add(labelPelicula);
        panelSeleccion.add(comboPeliculas);
        panelSeleccion.add(labelHora);
        panelSeleccion.add(comboHoras);
        panelSeleccion.add(labelAsientos);
        panelSeleccion.add(comboAsientos);
        panelSeleccion.add(btnConfirmarSeleccion);
      
        principal.add(panelSeleccion);
        principal.setVisible(true);
    }
    private void mostrarSala() {
    }
}
