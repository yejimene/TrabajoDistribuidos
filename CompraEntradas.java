import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;


public class CompraEntradas extends JFrame {

    private String idUnico;

    //PRE:
    //POS:Se crea y muestra una ventana con un formulario para ingresar el DNI y correo electrónico, además se configura un listener para manejar eventos del botón Confirmar.
    public CompraEntradas() {
        setTitle("Introducir Datos de Compra");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(5, 2, 10, 10));

        JLabel dniLabel = new JLabel("Introduce tu DNI:");
        JTextField dniField = new JTextField();
        JLabel correoLabel = new JLabel("Introduce tu correo:");
        JTextField correoField = new JTextField();
        JButton confirmarButton = new JButton("Confirmar");

        add(dniLabel);
        add(dniField);
        add(correoLabel);
        add(correoField);
        add(confirmarButton);

        confirmarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String dni = dniField.getText().trim();
                String correo = correoField.getText().trim();

                if (!dni.isEmpty() && verificarDNI(dni)) {
                    idUnico = dni;
                    System.out.println("DNI ingresado (idUnico): " + idUnico);
                    System.out.println("Correo ingresado: " + correo);
                    dispose();
                } else {
                    if(dni.isEmpty()){
                    JOptionPane.showMessageDialog(CompraEntradas.this, "Por favor, introduce un DNI.", "Error", JOptionPane.ERROR_MESSAGE);
                    }else {
                        JOptionPane.showMessageDialog(
                                CompraEntradas.this,
                                "Formato incorrecto para el DNI.\nDebe tener 8 dígitos seguidos de una letra mayúscula.\nEjemplo: 12345678Z",
                                "Error en el DNI",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            }
        });

        setVisible(true);
    }

    //PRE: El atributo idUnico debe haberse inicializado con un valor no nulo si el DNI fue ingresado correctamente.
    //POS: Devuelve el valor actual de idUnico, corresponde al DNI introducido en el constructor.
    public String getIdUnico() {
        return idUnico;
    }

    //PRE: dni != null
    //POS: Devuelve true si el formato de dni equivale a 8 dígitos seguidos de una letra mayúscula, false en caso contrario.
    private boolean verificarDNI(String dni) {
        return dni.matches("\\d{8}[A-Z]");
    }


}
