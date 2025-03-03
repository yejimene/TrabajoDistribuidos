
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ReservaAsientosCine {
    private JFrame principal = new JFrame();
    private String idUnico;
    private JComboBox<String> comboPeliculas;
    private JComboBox<String> comboHoras;
    private JComboBox<String> comboAsientos;
    private String numAsientosReserva;
    private JButton[][] asientos;
    private final int Filas = 5;
    private final int Columnas = 8;
    private final int precio_asiento = 10;
    private final ImageIcon asientoDisponible = redimensionarIcono(new ImageIcon("libre.png"), 30, 30);
    private final ImageIcon asientoOcupado = redimensionarIcono(new ImageIcon("ocupado.png"), 30, 30);
    private final ImageIcon asientoSeleccionado = redimensionarIcono(new ImageIcon("seleccionado.png"), 30, 30);
    private JButton btnConfirmarAsientos;
    private ReservaLogica reservaLogica = new ReservaLogica();
    private ReservaComunicacion reservaComunicacion = new ReservaComunicacion();

    //PRE:
    //POS: Se inicializa y muestra una ventana para seleccionar película, hora y cantidad de asientos.
    public ReservaAsientosCine() {
        principal.setTitle("Reserva Asientos Cine ");
        principal.setSize(400, 200);
        principal.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        principal.setLocationRelativeTo(null);
        // Panel de selección de pelicula y hora
        JPanel panelSeleccion = new JPanel();
        panelSeleccion.setLayout(new GridLayout(4, 2, 10, 10));
        JLabel labelPelicula = new JLabel("Seleccione la película:");
        String[] peliculas = {"Pelicula 1", "Pelicula 2", "Pelicula 3"};
        comboPeliculas = new JComboBox<>(peliculas);
        JLabel labelHora = new JLabel("Seleccione la hora:");
        String[] horas = {"14:00", "17:00", "20:00"};
        comboHoras = new JComboBox<>(horas);
        JLabel labelAsientos = new JLabel("Seleccione la cantidad de asientos:");
        String[] cantidadAsientos = {"1", "2", "3","4","5", "20"};
        comboAsientos = new JComboBox<>(cantidadAsientos);
        JButton btnConfirmarSeleccion = new JButton("Confirmar");
        btnConfirmarSeleccion.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CompraEntradas compraEntradas = new CompraEntradas();
                principal.setVisible(false);
                compraEntradas.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        idUnico = compraEntradas.getIdUnico();
                        if (idUnico != null) {
                            mostrarSala();
                        }else {
                            principal.setVisible(true);
                        }
                    }
                });
            }
        });
        JLabel precioTotal = new JLabel("Precio total: €0");
        comboAsientos.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int cantidad = Integer.parseInt((String) comboAsientos.getSelectedItem());
                precioTotal.setText("Precio total: €" + (cantidad * precio_asiento));
            }
        });

        // Componentes de seleccion
        panelSeleccion.add(labelPelicula);
        panelSeleccion.add(comboPeliculas);
        panelSeleccion.add(labelHora);
        panelSeleccion.add(comboHoras);
        panelSeleccion.add(labelAsientos);
        panelSeleccion.add(comboAsientos);
        panelSeleccion.add(precioTotal);
        panelSeleccion.add(btnConfirmarSeleccion);

        principal.add(panelSeleccion);
        principal.setVisible(true);
    }

    //PRE:
    //POS: Crea y muestra una nuevo Frame para seleccionar asientos.
    private void mostrarSala() {
        boolean idAceptada = reservaComunicacion.conectar("localhost", 55555, idUnico);
        if (!idAceptada) {
            JOptionPane.showMessageDialog(principal, "No se puede iniciar sesión con la misma ID en dos sesiones.");
            errorCerrar();
            return;
        }
        String pelicula = (String) comboPeliculas.getSelectedItem();
        String hora = (String) comboHoras.getSelectedItem();
        numAsientosReserva = (String) comboAsientos.getSelectedItem();
        String clave = pelicula + "_" + hora;

        JFrame sala = new JFrame("Sala de Cine - Selección de Asientos");
        sala.setSize(800, 600);
        sala.setLocationRelativeTo(null);
        sala.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                principal.setVisible(true);
                System.out.println(reservaLogica.getReservados());
                for (JButton boton : reservaLogica.getReservados()) {
                    if (boton.getIcon() == asientoSeleccionado) {
                        System.out.println(boton.getText());
                        reservaComunicacion.eliminarPrematuramente(boton);
                    }
                }
                liberarRecursos();
            }
        });

        JPanel panelPantalla = new JPanel();
        panelPantalla.setBackground(Color.LIGHT_GRAY);
        JLabel textoPantalla = new JLabel("Pantalla");
        panelPantalla.add(textoPantalla);

        JPanel panelAsientos = new JPanel(new BorderLayout());
        JPanel panelCentral = new JPanel(new GridLayout(Filas, Columnas));
        asientos = new JButton[Filas][Columnas];

        // Crear los asientos con la selección anterior y los ocupados
        for (int i = 0; i < Filas; i++) {
            for (int j = 0; j < Columnas; j++) {
                asientos[i][j] = new JButton((i + 1) + "-" + (j + 1));
                asientos[i][j].setIcon(asientoDisponible);
                asientos[i][j].setFocusable(false);

                // Si el asiento estaba reservado previamente, se marca como ocupado
                if (reservaLogica.getReservados().contains(asientos[i][j])) {
                    asientos[i][j].setIcon(asientoSeleccionado);
                }

                asientos[i][j].addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JButton boton = (JButton) e.getSource();
                        if (boton.getIcon() == asientoDisponible && (reservaLogica.getReservados().size() < Integer.parseInt(numAsientosReserva)) && reservaLogica.puedeComprarAsientos()) {
                            boton.setIcon(asientoSeleccionado);
                            reservaLogica.agregarAsiento(boton);
                            reservaComunicacion.enviarPrematuramente((JButton)e.getSource());
                        } else if (boton.getIcon() == asientoSeleccionado) {
                            boton.setIcon(asientoDisponible);
                            reservaLogica.quitarAsiento(boton);
                            reservaComunicacion.eliminarPrematuramente((JButton)e.getSource());
                        }
                        // Habilitar el botón si hay asientos seleccionados
                        reservaLogica.habilitarCompra(btnConfirmarAsientos, Integer.parseInt(numAsientosReserva));
                    }
                });
                panelCentral.add(asientos[i][j]);
            }
        }

        // Añadir el número de las filas
        JPanel panelFilas = new JPanel(new GridLayout(Filas, 1));
        for (int i = 0; i < Filas; i++) {
            panelFilas.add(new JLabel("Fila " + (i + 1)));
        }

        panelAsientos.add(panelFilas, BorderLayout.WEST);
        panelAsientos.add(panelCentral, BorderLayout.CENTER);

        // Pedir los asientos ocupados después de cargar todos
        int n = reservaComunicacion.pedirAsientos(clave, asientos, asientoOcupado);
        if (Integer.parseInt(numAsientosReserva) > ((Filas * Columnas)) - n) {
            JOptionPane.showMessageDialog(sala, "Superas el número de entradas disponibles queda: "+ (Filas*Columnas-n));
            errorCerrar();
            return;
        }

        // Botón para regresar a la pantalla anterior
        JButton btnCerrar = new JButton("Regresar");
        btnCerrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sala.dispose();
                principal.setVisible(true);
                for (JButton boton : reservaLogica.getReservados()) {
                    if (boton.getIcon() == asientoSeleccionado) {
                        System.out.println(boton.getText());
                        reservaComunicacion.eliminarPrematuramente(boton);
                    }
                }
                liberarRecursos();
            }
        });

        // Botón de confirmar asientos (siempre visible, pero habilitado dependiendo de los asientos seleccionados)
        btnConfirmarAsientos = new JButton("Comprar");
        btnConfirmarAsientos.setEnabled(false);
        btnConfirmarAsientos.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int total = reservaLogica.calcularCoste();
                int num = JOptionPane.showConfirmDialog(sala, "Confirmar la compra. Coste Total: €" + total, "Confirmación", JOptionPane.YES_NO_OPTION);
                if (num == JOptionPane.YES_OPTION) {
                    boolean compraExitosa = reservaComunicacion.comprarAsientos(reservaLogica.getReservados());
                    String mensaje;
                    if (compraExitosa) {
                        mensaje = "Compra realizada con éxito. Coste total: €" + total;
                        for (JButton asiento : reservaLogica.getReservados()) {
                            asiento.setIcon(asientoOcupado);
                        }
                        reservaLogica.limpiarReservas();
                        btnConfirmarAsientos.setEnabled(false);
                        reservaLogica.bloquearCompra();
                    } else {
                        mensaje = "Error: Algunos asientos ya están reservados";
                        JOptionPane.showMessageDialog(sala, mensaje);
                        sala.dispose();
                        errorCerrar();
                        JOptionPane.showMessageDialog(sala, "vuelve a iniciar sesion para coger asientos");
                        return;
                    }
                    JOptionPane.showMessageDialog(sala, mensaje);
                } else {
                    JOptionPane.showMessageDialog(sala, "Compra cancelada, puede seguir comprando otros asientos");
                }
            }
        });

        JPanel panelBotones = new JPanel();
        panelBotones.add(btnCerrar);
        panelBotones.add(btnConfirmarAsientos);

        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.add(panelPantalla, BorderLayout.NORTH);
        panelPrincipal.add(panelAsientos, BorderLayout.CENTER);
        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);
        sala.add(panelPrincipal);
        sala.setVisible(true);
        principal.setVisible(false);
    }

    //PRE:
    //POS: Se crea una instancia de la clase ReservaAsientosCine, inicializando así la interfaz gráfica.
    public static void main(String[] args) {
        ReservaAsientosCine r= new ReservaAsientosCine();
    }

    //PRE: icono != null && ancho > 0 && alto > 0
    //POS: Devuelve un ImageIcon con las dimensiones especificadas de ancho y alto.
    private ImageIcon redimensionarIcono(ImageIcon icono, int ancho, int alto) {
        Image imagen = icono.getImage();
        Image imagenRedimensionada = imagen.getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
        return new ImageIcon(imagenRedimensionada);
    }

    //PRE:
    //POS: Todos los asientos seleccionados son desmarcados y la conexión con el servidor está cerrada.
    private void liberarRecursos() {
        reservaLogica.limpiarReservas();
        reservaComunicacion.cerrarConexionAntes();
    }

    //PRE:
    //POS: La ventana principal se vuelve visible, todas las reservas pendientes se eliminan en reservaLogica y cualquier conexión activa en reservaComunicacion se cierra.
    private void errorCerrar(){
        principal.setVisible(true);
        reservaLogica.limpiarReservas();
        reservaComunicacion.cerrarConexion();
    }

}
