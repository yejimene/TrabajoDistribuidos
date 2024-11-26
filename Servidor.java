import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Servidor {
    public static void main(String args[]) {
        ExecutorService hilo= Executors.newCachedThreadPool();
        ServerSocket s= null;
        try{
            s= new ServerSocket(55555);
            while(!Thread.interrupted()) {
                try{
                    hilo.execute(new Procesar(s.accept()));
                }catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }catch(IOException  e) {
            e.printStackTrace();
        }finally{
            if(s!=null){
                try{
                    s.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
            hilo.shutdown();
        }

    }
}