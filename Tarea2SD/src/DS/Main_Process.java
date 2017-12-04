
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.rmi.Naming;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main_Process {

    private ArrayList<Inter> processes;

    void startServer() throws IOException {
        int processIndex = 0;
        Scanner sc = new Scanner(System.in);
        int n;
        System.out.println("Numero de procesos: ");
        n = sc.nextInt();
        try{
            writeFile2(n);
        }
        catch(IOException e){
            e.printStackTrace();
        }
        String[] urls = new String[n];
        int i;
        for (i=0;i<n;i++){
        urls[i] = "rmi://localhost/process"+i;}
        processes = new ArrayList<Inter>();
        for (String url : urls) {
            try {
                Inter process;
                process = new Server(urls, processIndex);
                new Thread((Server) process).start();
                Naming.bind(url, process);
                processIndex++;
                processes.add(process);

            } catch (RemoteException e1) {
                throw new RuntimeException(e1);
            } catch (AlreadyBoundException e2) {
                throw new RuntimeException(e2);
            } catch (MalformedURLException e4) {
                throw new RuntimeException(e4);
            }
        }
    }

    public List<Inter> getProcesses() {
        return processes;
    }

public static void writeFile2(int numero) throws IOException {
        FileWriter fw = new FileWriter("out.txt");

        fw.write(""+numero);
        

        fw.close();
    }

   
}


