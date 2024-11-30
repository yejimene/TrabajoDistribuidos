import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class Procesar implements Runnable {
    private Socket s;
    private static ConcurrentHashMap<String, Map<String, Vector<String>>> asientosUsuarios = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Vector<String>> Cine = new ConcurrentHashMap<>();
    private static Vector<String> usuariosActivos = new Vector<>();

    private String idUsuario;
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

            idUsuario = in.readLine();
            synchronized (usuariosActivos) {
                if (usuariosActivos.contains(idUsuario)) {
                    out.write("no\n");
                    out.flush();
                    return;
                } else {
                    out.write("si\n");
                    out.flush();
                    usuariosActivos.add(idUsuario);
                }
            }

            clave = in.readLine();
            enviarAsientosOcupados(clave, out);


            String id = in.readLine();

            while (id != null && !id.equals("Comprar")) {
                if (id.equals("ELIMINAR")) {
                    id = in.readLine();
                    deseleccionarAsiento(id, idUsuario);
                } else {
                    seleccionarAsiento(id, idUsuario);
                }
                id = in.readLine();
            }
            ArrayList<String> asientosDeseados = new ArrayList<>();
            String asiento;
            while ((asiento = in.readLine()) != null && !asiento.equals("FIN")) {
                asientosDeseados.add(asiento);
            }
            if (id != null && validarYComprar(idUsuario, asientosDeseados)) {
                System.out.println("si");
                    out.write("true\n");
                } else {
                cancelarSeleccion(idUsuario);
                    out.write("false\n");
                }
                out.flush();

            mostrarAsientosReservados();
            synchronized (usuariosActivos) {
                usuariosActivos.remove(idUsuario);
            }

        } catch (IOException e) {
            cancelarSeleccion(idUsuario);
            synchronized (usuariosActivos) {
                usuariosActivos.remove(idUsuario);
            }
            e.printStackTrace();
        } finally {
            cerrarTodo(out, in, s);
        }
    }

    public void seleccionarAsiento(String idAsiento, String usuario) {
        synchronized (asientosUsuarios) {
            asientosUsuarios.putIfAbsent(usuario, new ConcurrentHashMap<>());
            Map<String, Vector<String>> peliculas = asientosUsuarios.get(usuario);
            peliculas.putIfAbsent(clave, new Vector<>());

            if (!algunoContiene(idAsiento)) {
                peliculas.get(clave).add(idAsiento);
            }
        }
    }

    public void deseleccionarAsiento(String idAsiento, String usuario) {
        synchronized (asientosUsuarios) {
            Map<String, Vector<String>> peliculas = asientosUsuarios.get(usuario);
            if (peliculas != null && peliculas.containsKey(clave)) {
                Vector<String> asientosUsuario = peliculas.get(clave);
                asientosUsuario.remove(idAsiento);
            }
        }
    }

    public void cancelarSeleccion(String usuario) {
        synchronized (asientosUsuarios) {
            if(usuario!=null) {
                Map<String, Vector<String>> peliculas = asientosUsuarios.get(usuario);
                Vector<String> peliculas2= Cine.get(clave);
                if (peliculas != null && peliculas.containsKey(clave)) {
                    Vector<String> asientosUsuario = peliculas.get(clave);
                   for(String linea: asientosUsuario){
                       if(peliculas2!=null) {
                           if (!peliculas2.contains(linea)) {
                               asientosUsuario.remove(linea);
                           }
                       }
                   }

                }
            }

        }
    }


    public boolean validarYComprar(String usuario, ArrayList<String> asientosDeseados) {
        synchronized (Cine) {
            synchronized (asientosUsuarios) {
                Map<String, Vector<String>> peliculas = asientosUsuarios.get(usuario);
                if (!peliculas.containsKey(clave)) {
                    return false;
                }

                Vector<String> asientosUsuario = peliculas.get(clave);
                if (asientosUsuario.size() != asientosDeseados.size() || !asientosUsuario.containsAll(asientosDeseados)) {
                    return false;
                }
                for (String asiento : asientosDeseados) {
                    if (algunoContiene(asiento)) {
                        return false;
                    }
                }
                    Cine.putIfAbsent(clave, new Vector<>());
                Cine.get(clave).addAll(asientosDeseados);

                return true;
            }
        }
    }


    public boolean algunoContiene(String idAsiento) {
        synchronized (asientosUsuarios) {
            for (String usuario : asientosUsuarios.keySet()) {
                if (!usuario.equals(idUsuario)) {
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

            synchronized (asientosUsuarios) {
                for (Map<String, Vector<String>> peliculas : asientosUsuarios.values()) {
                    if (peliculas.containsKey(clave)) {
                        for (String asiento : peliculas.get(clave)) {
                            if (!ocupados.contains(asiento)) {
                                out.write(asiento + "\n");
                            }
                        }
                    }
                }
            }
            out.write("FIN\n");
            out.flush();
        }
    }


    public void mostrarAsientosReservados() {
        for (String usuario : asientosUsuarios.keySet()) {
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
