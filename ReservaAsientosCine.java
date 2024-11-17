import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ReservaAsientosCine {
    private JFrame principal= new JFrame();
    private JComboBox<String> comboPeliculas;
    private JComboBox<String> comboHoras;
    private JComboBox<String> comboAsientos;
    private String numAsientosReserva;
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private JButton[][] asientos;
    private final int Filas= 5;
    private final int Columnas = 8;
    private final int precio_asiento = 10;
    private final ImageIcon asientoDisponible = redimensionarIcono(new ImageIcon("libre.png"), 30, 30);
    private final ImageIcon asientoOcupado = redimensionarIcono(new ImageIcon("ocupado.png"), 30, 30);
    private final ImageIcon asientoSeleccionado = redimensionarIcono(new ImageIcon("seleccionado.png"), 30, 30);
    private ArrayList<JButton> reservados= new ArrayList<>();
    private JButton btnConfirmarAsientos;
    private boolean PuedeComprar=true;

    public ReservaAsientosCine() {
        principal.setTitle("Reserva Asientos Cine ");
        principal. setSize(400, 200);
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
        String[] cantidadAsientos = {"1", "2", "3","10"};
        comboAsientos= new JComboBox<>(cantidadAsientos);
        JButton btnConfirmarSeleccion = new JButton("Confirmar");
        btnConfirmarSeleccion.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mostrarSala();
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
    private void mostrarSala() {
        String pelicula = (String) comboPeliculas.getSelectedItem();
        String hora = (String) comboHoras.getSelectedItem();
        numAsientosReserva=(String) comboAsientos.getSelectedItem();
        String clave = pelicula + "_" + hora;
        JFrame Sala = new JFrame("Sala de Cine - Selección de Asientos");
        Sala.setSize(800, 600);
        Sala.setLocationRelativeTo(null);
        Sala. addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                liberarRecursos();
                principal.setVisible(true);
            }
        });

        try {
            socket = new Socket("localhost", 55555);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            JPanel panelPantalla = new JPanel();
            panelPantalla.setBackground(Color.LIGHT_GRAY);
            JLabel TextoPantalla = new JLabel("Pantalla");
            panelPantalla.add(TextoPantalla);

            JPanel panelAsientos = new JPanel(new BorderLayout());
            JPanel PanelCentral= new JPanel(new GridLayout(Filas, Columnas));
            asientos = new JButton[Filas][Columnas];
            for (int i = 0; i < Filas; i++) {
                for (int j = 0; j < Columnas; j++) {
                    asientos[i][j] = new JButton((i+1) + "-" + (j+1));
                    asientos[i][j].setIcon(asientoDisponible);
                    asientos[i][j].setFocusable(false);
                    asientos[i][j].addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            JButton boton = (JButton) e.getSource();
                            if (boton.getIcon() == asientoDisponible) {
                                boton.setIcon(asientoSeleccionado);
                                reservados.add(boton);
                            } else if (boton.getIcon() == asientoSeleccionado) {
                                boton.setIcon(asientoDisponible);
                                reservados.remove(boton);
                            }
                            gestionarBotonCompra();
                        }
                    });
                    PanelCentral.add(asientos[i][j]);
                }
            }
            // Para añadir el numero de las filas
            JPanel panelFilas = new JPanel(new GridLayout(Filas, 1));
            for (int i = 0; i < Filas; i++) {
                panelFilas.add(new JLabel("Fila " + (i+1)));
            }
            // añade al panel de los asientos(que engloba tanto los asientos como las filas), los asientos y las filas numeradas
            panelAsientos.add(panelFilas, BorderLayout.WEST);
            panelAsientos.add(PanelCentral, BorderLayout.CENTER);
            //pedimos los asientos disponibles despues de cargar todos.
            int n=pedirAsientos(clave,in,out);
            if(Integer.parseInt(numAsientosReserva)>((Filas*Columnas))-n) {
                JOptionPane.showMessageDialog(Sala, "Superas el numero de entradas disponibles");
            }
            //boton para regresar a la pantalla anterior
            JButton btnCerrar = new JButton("Regresar");
            btnCerrar.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    liberarRecursos();
                    Sala.dispose();
                    principal.setVisible(true);

                } });

            // boton de confirmar asientos (siempre visible, pero habilitado dependiendo de los asientos seleccionados)
            btnConfirmarAsientos = new JButton("Comprar");
            btnConfirmarAsientos.setEnabled(false);
            btnConfirmarAsientos.addMouseListener(new MouseAdapter() {
                @Override
                public void  mouseClicked(MouseEvent e) {
                    int total = calcularCoste();
                    int num =  JOptionPane.showConfirmDialog(Sala, "Confirmar la compra. Coste Total: €" + total, "Confirmacion", JOptionPane.YES_NO_OPTION);
                    if (num == JOptionPane.YES_OPTION) {
                        boolean compraExitosa = comprar(in, out);
                        String mensaje;
                        if (compraExitosa) {
                            mensaje = "Compra realizada con éxito. Coste total: €" + total;
                            for (JButton asiento : reservados) {
                                asiento.setIcon(asientoOcupado);
                            }
                            reservados.clear();
                            btnConfirmarAsientos.setEnabled(false);
                            PuedeComprar = false;
                        }else{
                            mensaje = "Error: Algunos asientos ya están reservados, puede elegir otros";
                        }
                        JOptionPane.showMessageDialog(Sala, mensaje);
                    }else{
                        JOptionPane.showMessageDialog(Sala, "Compra cancelada, puede seguir comprando otros asientos");
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
            Sala.add(panelPrincipal);
            Sala.setVisible(true);
            principal.setVisible(false);

        }catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    public static void main(String[] args) {
        ReservaAsientosCine reserva= new ReservaAsientosCine();
    }
    private ImageIcon redimensionarIcono(ImageIcon icono, int ancho, int alto) {
        Image imagen = icono.getImage();
        Image imagenRedimensionada = imagen.getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
        return new ImageIcon(imagenRedimensionada);
    }
    private boolean comprar(BufferedReader in,BufferedWriter out) {
        try{
            for (JButton s : reservados) {
                System.out.println(s.getText());
                out.write(s.getText() + "\n");
            }
            out.write("ACABADO\n");
            out.flush();
            return in.readLine().equals("true");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private int calcularCoste() {
        return reservados.size() * precio_asiento;
    }
    private void gestionarBotonCompra() {
        // Habilitar el boton si hay asientos seleccionados
        btnConfirmarAsientos.setEnabled(SeleccionoTodos());
    }

    private boolean SeleccionoTodos() {
        return reservados.size()==Integer.parseInt(numAsientosReserva)&& PuedeComprar;
    }
    private int pedirAsientos(String clave,BufferedReader in,BufferedWriter out) {
        int numero=0;
        try {
            out.write(clave + "\n");
            out.flush();
            String asiento;
            while ((asiento = in.readLine()) != null && !asiento.equals("NADA") && !asiento.equals("FIN")) {
                numero++;
                String[] pos = asiento.split("-");
                int fila = Integer.parseInt(pos[0])-1 ;
                int columna = Integer.parseInt(pos[1])-1;
                asientos[fila][columna].setIcon(asientoOcupado);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return numero;
    }
    private void liberarRecursos() {
        reservados.clear();
        PuedeComprar=true;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
