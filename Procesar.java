import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

public class Procesar implements Runnable {
    Socket s;
    private static ConcurrentHashMap<String, Vector<String>> Cine = new ConcurrentHashMap<String, Vector<String>>();
    private List<String> lista= new ArrayList<>();
    private String clave;
    public Procesar(Socket s) {
        this.s= s;
    }
    public void run() {
        try(BufferedReader in =new BufferedReader(new InputStreamReader(s.getInputStream())) ;
            DataOutputStream out= new DataOutputStream(s.getOutputStream())){
            String id;
            clave= in.readLine();
            enviarAsientosOcupados(clave);
            while(!(id=in.readLine()).equals("ACABADO")) {
                lista.add(id);
            }
            if(comprar(lista)) {
                out.writeBoolean(true);
            }else {
                out.writeBoolean(false);
            }
        }catch(IOException e){
            e.printStackTrace();
        }finally {
            try {
                s.close();
            }catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized boolean comprar(List<String> s) {
        Vector<String> vector= Cine.get(clave);
        for(String linea: vector) {
            for(String s1: s) {
                if(linea.equals(s1)) {
                    return false;
                }
            }
        }
        for(String s1: s) {
            vector.add(s1);

        }
        return true;
    }
    public void enviarAsientosOcupados(String clave) {
        try(BufferedWriter out =new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))){
            for(String s : Cine.get(clave)) {
                out.write(s+"\n");
            }
            out.flush();
        }catch(IOException e) {
            e.printStackTrace();
        }
    }
}
