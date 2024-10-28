import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;

public class Procesar implements Runnable {
    Socket s;
    private static Vector<String> ButacasCompradas= new Vector<>();
    private List<String> lista= new ArrayList<>();
    public Procesar(Socket s) {
        this.s= s;
    }
    public void run() {
        try(BufferedReader in =new BufferedReader(new InputStreamReader(s.getInputStream())) ;
            DataOutputStream out= new DataOutputStream(s.getOutputStream())){
            String id;
            while((id=in.readLine())!=null) {
                lista.add(id);
            }
            if(estaDisponible(lista)) {
                ComprarButacas(lista);
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

    public synchronized boolean estaDisponible(List<String> in) {
        for(String id: in) {
            if(ButacasCompradas.contains(id)) {
                return false;
            }
        }
        return true;
    }
    public synchronized void ComprarButacas(List<String> in)  {
        for(String id: in) {
            ButacasCompradas.add(id);
        }
    }



}