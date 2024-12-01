
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.*;

public class ReservaComunicacion {
    private Socket socket;
    private BufferedReader in=null;
    private BufferedWriter out=null;

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
    public void enviarPrematuramente(JButton boton,String id){
        try {
            out.write(boton.getText()+"\n");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void eliminarPrematuramente(JButton boton,String id){
        try {
            out.write("ELIMINAR\n");
            out.write(boton.getText() + "\n");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
