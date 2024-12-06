import java.util.ArrayList;
import javax.swing.*;

public class ReservaLogica {
    private final int precio_asiento = 10;
    private ArrayList<JButton> reservados = new ArrayList<>();
    private boolean puedeComprar= true;

    //PRE:
    //POS: Devuelve el coste total de la reserva, multiplicando el número de asientos reservados por el precio por asiento.
    public int calcularCoste() {
        return reservados.size() * precio_asiento;
    }

    //PRE: boton != null
    //POS: boton es agregado a la lista de asientos reservados
    public void agregarAsiento(JButton boton) {
        reservados.add(boton);
    }

    //PRE: boton != null
    //POS: Si el botn se encuentra en la lista, es removido, en caso contrario no se hace nada.
    public void quitarAsiento(JButton boton) {
        reservados.remove(boton);
    }

    //PRE: numAsientosReserva > 0
    //POS: Devuelve true si el número de asientos reservados es igual al número de asientos que se quiere reservar y se pueden comprar, false en caso contrario.
    public boolean seleccionoTodos(int numAsientosReserva) {
        return reservados.size() == numAsientosReserva && puedeComprar;
    }

    //PRE: btnConfirmarAsientos != null && numAsientosReserva > 0
    //POS: Habilita o deshabilita el botón btnConfirmarAsientos dependiendo de si todos los asientos requeridos han sido seleccionados y si la compra es posible
    public void habilitarCompra(JButton btnConfirmarAsientos, int numAsientosReserva) {
        btnConfirmarAsientos.setEnabled(seleccionoTodos(numAsientosReserva));
    }

    //PRE:
    //POS: Devuelve el valor actual de la variable puedeComprar
    public boolean puedeComprarAsientos(){
        return puedeComprar;
    }

    //PRE:
    //POS: elimina todos los asientos reservados y restaura el estado de puedeComprar a true
    public void limpiarReservas() {
        reservados.clear();
        puedeComprar = true;
    }

    //PRE:
    //POS: Devuelve la lista de asientos reservados
    public ArrayList<JButton> getReservados() {
        return reservados;
    }

    //PRE:
    //POS: Cambia el valor de puedeComprar a false
    public void bloquearCompra() {
        puedeComprar = false;
    }
}
