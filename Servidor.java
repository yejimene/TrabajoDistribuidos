import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Servidor {
    public static void main(String args[]) {
        try(ServerSocket s= new ServerSocket(55555)){
            ExecutorService hilo= Executors.newCachedThreadPool();
            while(true) {
                try{
                    hilo.execute(new Procesar(s.accept()));
                }catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }catch(IOException  e) {
            e.printStackTrace();
        }

    }
}