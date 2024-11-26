import java.util.ArrayList;
import javax.swing.*;

public class ReservaLogica {
    private final int precio_asiento = 10;
    private ArrayList<JButton> reservados = new ArrayList<>();
    private boolean puedeComprar = true;

    public int calcularCoste() {
        return reservados.size() * precio_asiento;
    }

    public void agregarAsiento(JButton boton) {
        reservados.add(boton);
    }

    public void quitarAsiento(JButton boton) {
        reservados.remove(boton);
    }

    public boolean seleccionoTodos(int numAsientosReserva) {
        return reservados.size() == numAsientosReserva && puedeComprar;
    }

    public void habilitarCompra(JButton btnConfirmarAsientos, int numAsientosReserva) {
        btnConfirmarAsientos.setEnabled(seleccionoTodos(numAsientosReserva));
    }

    public void limpiarReservas() {
        reservados.clear();
        puedeComprar = true;
    }

    public ArrayList<JButton> getReservados() {
        return reservados;
    }

    public void bloquearCompra() {
        puedeComprar = false;
    }
}
