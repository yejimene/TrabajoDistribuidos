import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class Servidor {
    public static void main(String args[]) {
        try(ServerSocket s= new ServerSocket(55555)){
            ExecutorService hilo= Executors.newCachedThreadPool();
            while(true) {
                try{
                    hilo.execute(new Procesar(s.accept()));
                }catch(RejectedExecutionException | NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }catch(IOException  e) {
            e.printStackTrace();
        }

    }
}