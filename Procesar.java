import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class Procesar implements Runnable {
    private Socket s;
    private static ConcurrentHashMap<String,Boolean> asientosPrematuros = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Vector<String>> Cine = new ConcurrentHashMap<>();
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
            id = in.readLine();
            while(id!=null &&!id.equals("COMPRAR") ) {
                if(id.equals("ELIMINAR")){
                    while (!id.startsWith("ACABADO")) {

                    }
                }
                while (!id.startsWith("ACABADO")) {
                    System.out.println(id);
                    if(!asientosPrematuros.contains(id)){
                        asientosPrematuros.put(id,true);
                    }
                    System.out.println(asientosPrematuros.size());
                    id=in.readLine();
                }

                id= in.readLine();
            }
            if(comprar(asientosPrematuros)){
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

    public boolean comprar(ConcurrentHashMap<String,Boolean> s) {
        synchronized (Cine) {
            Vector<String> vector = Cine.get(clave);
            for (String linea : vector) {
                for (String s1 : s.keySet()) {
                    if (linea.equals(s1)) {
                        return false;
                    }
                }
            }
            for (String s1 :  s.keySet()) {
                vector.add(s1);

            }
            return true;
        }
    }
    public void enviarAsientosOcupados(String clave, BufferedWriter out) throws IOException {
        Cine.putIfAbsent(clave, new Vector<>());
        if(Cine.get(clave).isEmpty() && asientosPrematuros.isEmpty()) {
            System.out.println();
            out.write("NADA"+"\n");
            out.flush();
        }else {
            for(String s : Cine.get(clave)) {
                out.write(s+"\n");
            }
            for(String s: asientosPrematuros.keySet()){
                System.out.println(s);
                out.write(s+"\n");
            }
            out.write("FIN\n");
            out.flush();
        }
    }
}
