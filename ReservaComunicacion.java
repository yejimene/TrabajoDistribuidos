
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.*;

public class ReservaComunicacion {
    private Socket socket;
    private BufferedReader in=null;
    private BufferedWriter out=null;

    //PRE: host no debe ser null ni vacío, puerto > 0 y id no debe ser null ni vacío
    //POS: Establece una conexión con el servidor con el socket, envía el id y recibe una respuesta del servidor. Devuelve true si la conexión fue exitosa y el servidor respondió con un si, false en caso contrario.
    public boolean conectar(String host, int puerto,String id) {
        try {
            socket = new Socket(host, puerto);
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out.write(id+"\n");
            out.flush();
            String linea=in.readLine();
            System.out.println(linea);
            return linea.equals("si");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    //PRE: clave no debe ser null ni vacío, asientos != null y iconoOcupado != null
    //POS: Solicita los asientos ocupados al servidor, actualiza la interfaz gráfica con los asientos ocupados y devuelve el número de asientos ocupados
    public int pedirAsientos(String clave, JButton[][] asientos, ImageIcon iconoOcupado) {
        int numero = 0;
        try {
            out.write(clave + "\n");
            out.flush();
            String asiento;
            while ((asiento = in.readLine()) != null && !asiento.equals("NADA") && !asiento.equals("FIN")) {
                numero++;
                String[] pos = asiento.split("-");
                int fila = Integer.parseInt(pos[0]) - 1;
                int columna = Integer.parseInt(pos[1]) - 1;
                asientos[fila][columna].setIcon(iconoOcupado);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return numero;
    }

    //PRE: reservados no debe ser null ni vacío.
    //POS: Envía los asientos seleccionados al servidor, si la compra es exitosa devuelve true y false en caso contrario.
    public boolean comprarAsientos(ArrayList<JButton> reservados) {
        try {
            System.out.println("COmpramos");
            out.write("Comprar\n");
            for(JButton s: reservados){
                out.write(s.getText()+"\n");
            }
            out.write("FIN\n");
            out.flush();
            return in.readLine().equals("true");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    //PRE: 
    //POS: Cierra los flujos de entrada y salida y termina la conexión.
    public void cerrarConexionAntes() {
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (socket != null && !socket.isClosed()) {
                    try {
                        socket.shutdownOutput();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    //PRE: 
    //POS: Cierra la conexión con el servidor, cerrando los flujos y el socket.
    public void cerrarConexion() {
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    //PRE: boton != null
    //POS: Envía el texto del botón preseleccionandolo para que nadie más lo pueda seleccionar.
    public void enviarPrematuramente(JButton boton){
        try {
            out.write(boton.getText()+"\n");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //PRE: boton no debe ser null
    //POS: Envía una solicitud al servidor para eliminar el asiento asociado al botón
    public void eliminarPrematuramente(JButton boton){
        try {
            out.write("ELIMINAR\n");
            out.write(boton.getText() + "\n");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
