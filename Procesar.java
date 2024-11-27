import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class Procesar implements Runnable {
    private Socket s;
    private static ConcurrentHashMap<String, Vector<String>> Cine = new ConcurrentHashMap<String, Vector<String>>();
    private List<String> lista= new ArrayList<>();
    private String clave;
    public Procesar(Socket s) {
        this.s= s;
    }
    public void run() {
        BufferedWriter out=null;
        BufferedReader in=null;
        try{
            out =new BufferedWriter(new OutputStreamWriter(s.getOutputStream(),"UTF-8"));
            in =new BufferedReader(new InputStreamReader(s.getInputStream(),"UTF-8"));
            String id;
            clave= in.readLine();
            System.out.println(clave);
            enviarAsientosOcupados(clave,out);
            while((id = in.readLine()) != null &&!id.equals("ACABADO")) {
                System.out.println(id);
                lista.add(id);
            }
            if(comprar(lista)){
                System.out.println("true");
                out.write("true\n");
                out.flush();
            }else {
                System.out.println("false");
                out.write("false\n");
                out.flush();
            }
        }catch(IOException e){
            e.printStackTrace();
        }finally {
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

    public boolean comprar(List<String> s) {
        synchronized (Cine) {
            Vector<String> vector = Cine.get(clave);
            for (String linea : vector) {
                for (String s1 : s) {
                    if (linea.equals(s1)) {
                        return false;
                    }
                }
            }
            for (String s1 : s) {
                vector.add(s1);

            }
            return true;
        }
    }
    public void enviarAsientosOcupados(String clave, BufferedWriter out) throws IOException {
        Cine.putIfAbsent(clave, new Vector<>());
        if(Cine.get(clave).isEmpty()) {
            out.write("NADA"+"\n");
            out.flush();
        }else {
            for(String s : Cine.get(clave)) {
                out.write(s+"\n");
            }
            out.write("FIN\n");
            out.flush();
        }
    }
}
