import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class Procesar implements Runnable {
    private Socket s;
    private static ConcurrentHashMap<String, Map<String, Vector<String>>> asientosUsuarios = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Vector<String>> Cine = new ConcurrentHashMap<>();
    private static Vector<String> usuariosActivos = new Vector<>();
    private BufferedReader in=null;
    private BufferedWriter out=null;
    private String idUsuario;
    private String clave;

    public Procesar(Socket s) {
        this.s = s;
    }

    public void run() {
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
            enviarAsientosOcupados();


            String id = in.readLine();

            while (id != null && !id.equals("Comprar")) {
                if (id.equals("ELIMINAR")) {
                    id = in.readLine();
                    deseleccionarAsiento(id);
                } else {
                    seleccionarAsiento(id);
                }
                id = in.readLine();
            }
            ArrayList<String> asientosDeseados = new ArrayList<>();
            String asiento;
            while ((asiento = in.readLine()) != null && !asiento.equals("FIN")) {
                asientosDeseados.add(asiento);
            }
            if (id != null && validarYComprar( asientosDeseados)) {
                System.out.println("si");
                out.write("true\n");
            } else {
                cancelarSeleccion();
                out.write("false\n");
            }
            out.flush();

            mostrarAsientosReservados();
                usuariosActivos.remove(idUsuario);
        } catch (IOException e) {
            cancelarSeleccion();
                usuariosActivos.remove(idUsuario);
            e.printStackTrace();
        } finally {
            cerrarTodo();
        }
    }

    public void seleccionarAsiento(String idAsiento) {
        // comprueba si alguno ya tiene ese asiento seleccinado y si ya lo tiene no lo selecciona
        synchronized (asientosUsuarios) {
            asientosUsuarios.putIfAbsent(idUsuario, new ConcurrentHashMap<>());
            Map<String, Vector<String>> peliculas = asientosUsuarios.get(idUsuario);
            peliculas.putIfAbsent(clave, new Vector<>());
            if (!algunoContiene(idAsiento)) {
                peliculas.get(clave).add(idAsiento);
            }
        }
    }

    public void deseleccionarAsiento(String idAsiento) {
        //deselecciona el asiento
        synchronized (asientosUsuarios) {
            Map<String, Vector<String>> peliculas = asientosUsuarios.get(idUsuario);
                Vector<String> asientosUsuario = peliculas.get(clave);
                asientosUsuario.remove(idAsiento);
        }
    }

    public void cancelarSeleccion() {
        //quita los asientos que no tenga comprados.
        synchronized (asientosUsuarios) {
                Map<String, Vector<String>> peliculas = asientosUsuarios.get(idUsuario);
                Vector<String> peliculas2= Cine.get(clave);
                if(peliculas!=null) {
                    Vector<String> asientosUsuario = peliculas.get(clave);
                    if (peliculas2 != null && asientosUsuario != null) {
                        Iterator<String> iterator = asientosUsuario.iterator();
                        while (iterator.hasNext()) {
                            String linea = iterator.next();
                            if (!peliculas2.contains(linea)) {
                                iterator.remove();
                            }
                        }
                    } else if (peliculas2 == null) {
                        asientosUsuario.removeAllElements();
                    }
                }
        }

    }


    public boolean validarYComprar( ArrayList<String> asientosDeseados) {
        // compra los asientos(añade a Cine)
        synchronized (Cine) {
            synchronized (asientosUsuarios) {
                Map<String, Vector<String>> peliculas = asientosUsuarios.get(idUsuario);
                Vector<String> asientosUsuario = peliculas.get(clave);// basta con mirar el size ya que no añade a la coleccion si esta ya seleccionado, no haria falta comprobar si tiene los asientos seleccionados ya
                if (asientosUsuario.size() != asientosDeseados.size()) {
                    return false;
                }
                Cine.putIfAbsent(clave, new Vector<>());
                Cine.get(clave).addAll(asientosDeseados);

                return true;
            }
        }
    }


    public boolean algunoContiene(String idAsiento) {
        //comprueba si alguno que sea el propio usuario tenga ese asiento.
            for (String usuario : asientosUsuarios.keySet()) {
                if (!usuario.equals(idUsuario)) {
                    Map<String, Vector<String>> peliculas = asientosUsuarios.get(usuario);
                    if (peliculas!=null&& peliculas.containsKey(clave)&& peliculas.get(clave).contains(idAsiento)) {
                        return true;// ha sido seleccionado por otro usuario
                    }
                }
            }
            return false;
        }


    public void enviarAsientosOcupados() throws IOException {
        //enviar asientos comprados
        synchronized (Cine) {
            Vector<String> ocupados = Cine.getOrDefault(clave, new Vector<>());

            for (String asiento : ocupados) {
                out.write(asiento + "\n");
            }// enviar asientos seleccionador pero no comprados
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

    public void cerrarTodo() {
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
                if (s != null) {
                    try {
                        s.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }
}