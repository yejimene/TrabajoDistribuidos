import java.io.*;
import java.net.Socket;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class Procesar implements Runnable {
    private Socket s;
    private static ConcurrentHashMap<Integer, Vector<String>> asientosUsuarios = new ConcurrentHashMap<>();
    private int idUsuario;
    private static ConcurrentHashMap<String, Vector<String>> Cine = new ConcurrentHashMap<>();
    private String clave;

    public Procesar(Socket s) {
        this.s = s;
    }

    public void run() {
        BufferedWriter out = null;
        BufferedReader in = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), "UTF-8"));
            in = new BufferedReader(new InputStreamReader(s.getInputStream(), "UTF-8"));
            String id;
            clave = in.readLine();
            System.out.println(clave);
            enviarAsientosOcupados(clave, out);
            id = in.readLine();
            System.out.println(id);
            while (id != null && !id.equals("Comprar")) {
                System.out.println(id);
                idUsuario = Integer.parseInt(id);
                id = in.readLine();
                if (id.equals("ELIMINAR")) {
                    id = in.readLine();
                    System.out.println(id);
                    if (!algunoContiene(id)) {
                        asientosUsuarios.get(idUsuario).remove(id);
                    }
                    System.out.println(asientosUsuarios.size());
                } else {
                    System.out.println(id);
                    if (!algunoContiene(id)) {
                        asientosUsuarios.computeIfAbsent(idUsuario, k -> new Vector<>()).add(id);
                    }
                    System.out.println(asientosUsuarios.size());
                }

                id = in.readLine();
            }

            // Lógica de compra con posibilidad de elegir nuevos asientos
            boolean compraExitosa = false;
            while (!compraExitosa) {
                if (id != null && comprar(idUsuario)) {
                    System.out.println("Compra exitosa");
                    out.write("true\n");
                    out.flush();
                    compraExitosa = true;  // Salir del bucle si la compra es exitosa
                } else {
                    // Si la compra no fue exitosa, eliminamos los asientos seleccionados y permitimos elegir nuevos
                    asientosUsuarios.remove(idUsuario);
                    out.write("false\n");
                    out.flush();
                    System.out.println("Compra fallida. Selecciona nuevos asientos.");
                    // Enviar los asientos ocupados nuevamente para que el usuario pueda elegir otros
                    enviarAsientosOcupados(clave, out);

                    // Pedir al usuario que seleccione nuevos asientos
                    id = in.readLine();  // Nuevamente leer la siguiente acción del cliente (selección de asientos)
                    if (id == null || id.equals("CANCELAR")) {
                        // Si el usuario decide cancelar la compra, salimos del bucle sin reiniciar todo
                        System.out.println("El usuario ha cancelado la compra.");
                        break;
                    } else {
                        // Si no es "CANCELAR", proceder con la nueva selección de asientos
                        idUsuario = Integer.parseInt(id); // Actualizar el ID del usuario si es necesario
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            cerrarTodo(out, in, s);
        }
    }

    public void cerrarTodo(BufferedWriter out, BufferedReader in, Socket s) {
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (s != null) {
            try {
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean algunoContiene(String id) {
        for (Vector<String> asientos : asientosUsuarios.values()) {
            if (asientos.contains(id)) {
                return true;
            }
        }
        return false;
    }

    public boolean comprar(int num) {
        synchronized (Cine) {
            // Verifica si el usuario tiene asientos
            if (asientosUsuarios.get(num) == null || asientosUsuarios.get(num).isEmpty()) {
                return false;  // No tiene asientos seleccionados
            }

            Vector<String> vector = Cine.get(clave);
            boolean compraValida = true;

            // Verifica si alguno de los asientos ya está ocupado
            for (String s1 : asientosUsuarios.get(num)) {
                if (vector.contains(s1)) {
                    compraValida = false;
                    break;
                }
            }

            // Si todos los asientos están libres, agregarlos al Cine
            if (compraValida) {
                for (String s1 : asientosUsuarios.get(num)) {
                    vector.add(s1);
                }
            }

            return compraValida;
        }
    }

    public void enviarAsientosOcupados(String clave, BufferedWriter out) throws IOException {
        Cine.putIfAbsent(clave, new Vector<>());
        if (Cine.get(clave).isEmpty() && asientosUsuarios.isEmpty()) {
            System.out.println();
            out.write("NADA" + "\n");
            out.flush();
        } else {
            for (String s : Cine.get(clave)) {
                out.write(s + "\n");
            }
            for (Vector<String> asientos : asientosUsuarios.values()) {
                for (String s : asientos) {
                    out.write(s + "\n");
                }
            }
            out.write("FIN\n");
            out.flush();
        }
    }
}
