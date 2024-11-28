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
                    System.out.println(asientosUsuarios.get(idUsuario).size());
                } else {
                    System.out.println(id);
                    if (!algunoContiene(id)) {
                        if (!asientosUsuarios.containsKey(idUsuario)) {
                            asientosUsuarios.put(idUsuario, new Vector<>());
                        }
                        asientosUsuarios.get(idUsuario).add(id);
                    }
                    System.out.println(asientosUsuarios.get(idUsuario).size());
                }

                id = in.readLine();
            }

            if (id != null && comprar(idUsuario)) {
                System.out.println("true");
                out.write("true\n");
                out.flush();
            } else {
                asientosUsuarios.remove(idUsuario);
                System.out.println("false");
                out.write("false\n");
                out.flush();
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

        for (Integer usuario : asientosUsuarios.keySet()) {

            if (usuario != idUsuario) {
                Vector<String> asientos = asientosUsuarios.get(usuario);
                if (asientos.contains(id)) {
                    return true;
                }
            }
        }
        return false;  // Si no se encuentra el asiento ocupado por otro usuario, retornamos false
    }


    public boolean comprar(int num) {
        synchronized (Cine) {
            // Verifica si el usuario tiene asientos reservados
            if (asientosUsuarios.get(num) == null || asientosUsuarios.get(num).isEmpty()) {
                return false;
            }

            Vector<String> vector = Cine.get(clave);
            boolean compraValida = true;

            // Verifica si alguno de los asientos reservados ya está ocupado en el cine
            for (String s1 : asientosUsuarios.get(num)) {
                if (vector.contains(s1)) {
                    compraValida = false;
                    break;
                }
            }

            if (compraValida) {
                // Si la compra es válida, actualiza los asientos ocupados
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
            out.write("NADA" + "\n");
            out.flush();
        } else {
            // Enviar los asientos ocupados por el cine
            for (String s : Cine.get(clave)) {
                out.write(s + "\n");
            }
            // Enviar los asientos ocupados por los usuarios
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
