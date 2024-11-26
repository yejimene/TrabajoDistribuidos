import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.*;

public class ReservaComunicacion {
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;

    public boolean conectar(String host, int puerto) {
        try {
            socket = new Socket(host, puerto);
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

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

    public boolean comprarAsientos(ArrayList<JButton> reservados) {
        try {
            for (JButton boton : reservados) {
                out.write(boton.getText() + "\n");
            }
            out.write("ACABADO\n");
            out.flush();
            return in.readLine().equals("true");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void cerrarConexion() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
