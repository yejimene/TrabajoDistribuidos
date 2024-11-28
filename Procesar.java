import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class Procesar implements Runnable {
    private Socket s;
    private static ConcurrentHashMap<Integer, Map<String, Vector<String>>> asientosUsuarios = new ConcurrentHashMap<>();
    private int idUsuario;
    private static ConcurrentHashMap<String, Vector<String>> Cine = new ConcurrentHashMap<>();
    private boolean puedeComprar = true;
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
            enviarAsientosOcupados(clave, out);
            id = in.readLine();

            while (id != null && !id.equals("Comprar")) {
                idUsuario = Integer.parseInt(id); // Identificar al usuario
                id = in.readLine();

                if (id.equals("ELIMINAR")) {
                    id = in.readLine(); // Asiento a eliminar
                    deseleccionarAsiento(id, idUsuario);
                } else {
                    seleccionarAsiento(id, idUsuario);
                }

                id = in.readLine();
            }

            if (id != null && validarYComprar(idUsuario)) {
                out.write("true\n");
            } else {
                cancelarSeleccion(idUsuario);
                out.write("false\n");
            }
            out.flush();
            mostrarAsientosReservados();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            cerrarTodo(out, in, s);
        }
    }

    public void seleccionarAsiento(String idAsiento, int usuario) {
        synchronized (asientosUsuarios) {
            if (!algunoContiene(idAsiento) && puedeComprar) {
                asientosUsuarios.putIfAbsent(usuario, new ConcurrentHashMap<>());
                Map<String, Vector<String>> peliculas = asientosUsuarios.get(usuario);
                peliculas.putIfAbsent(clave, new Vector<>());
                peliculas.get(clave).add(idAsiento);
            } else {
                puedeComprar = false;
            }
        }
    }


    public void deseleccionarAsiento(String idAsiento, int usuario) {
        synchronized (asientosUsuarios) {
            if (!algunoContiene(idAsiento)) {
                Map<String, Vector<String>> peliculas = asientosUsuarios.get(usuario);
                if (peliculas != null && peliculas.containsKey(clave)) {
                    Vector<String> asientosUsuario = peliculas.get(clave);
                    asientosUsuario.remove(idAsiento);
                    if (asientosUsuario.isEmpty()) {
                        peliculas.remove(clave);
                    }
                    puedeComprar = true;
                }
            } else {
                puedeComprar = false;
            }
        }
    }

    public boolean validarYComprar(int usuario) {
        Map<String, Vector<String>> peliculas = asientosUsuarios.get(usuario);
        if (peliculas == null || !peliculas.containsKey(clave) || peliculas.get(clave).isEmpty() || !puedeComprar) {
            return false;
        }
        Cine.putIfAbsent(clave, new Vector<>());
        Cine.get(clave).addAll(peliculas.get(clave));
        return true;
    }


    public void cancelarSeleccion(int usuario) {
        synchronized (asientosUsuarios) {
            asientosUsuarios.remove(usuario);
        }
    }


    public boolean algunoContiene(String idAsiento) {
        synchronized (asientosUsuarios) {
            for (int usuario : asientosUsuarios.keySet()) {
                if (usuario != idUsuario) {
                    Map<String, Vector<String>> peliculas = asientosUsuarios.get(usuario);
                    if (peliculas != null && peliculas.containsKey(clave) && peliculas.get(clave).contains(idAsiento)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }


    public void enviarAsientosOcupados(String clave, BufferedWriter out) throws IOException {
        synchronized (Cine) {
            Vector<String> ocupados = Cine.getOrDefault(clave, new Vector<>());
            for (String asiento : ocupados) {
                out.write(asiento + "\n");
            }
        }
        synchronized (asientosUsuarios) {
            for (Map<String, Vector<String>> peliculas : asientosUsuarios.values()) {
                if (peliculas.containsKey(clave)) {
                    for (String asiento : peliculas.get(clave)) {
                        out.write(asiento + "\n");
                    }
                }
            }
        }
        out.write("FIN\n");
        out.flush();
    }


    public void mostrarAsientosReservados() {
        for (Integer usuario : asientosUsuarios.keySet()) {
            Map<String, Vector<String>> peliculas = asientosUsuarios.get(usuario);
            if (peliculas != null && !peliculas.isEmpty()) {
                for (String clave : peliculas.keySet()) {
                    Vector<String> asientos = peliculas.get(clave);
                    System.out.print("Usuario " + usuario + " ha reservado los asientos para la película y hora " + clave + ": ");
                    if (asientos != null && !asientos.isEmpty()) {
                        for (String asiento : asientos) {
                            System.out.print(asiento + " ");
                        }
                    }
                    System.out.println();
                }
            }
        }
    }



    public void cerrarTodo(BufferedWriter out, BufferedReader in, Socket s) {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (s != null) s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
