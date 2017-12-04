import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Scanner;
import java.rmi.Naming;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.util.concurrent.TimeUnit;


public class Process {
    static private List<Inter> processes;
    static private String[] urls;
    public static void main(String[] args) throws RemoteException {

        System.out.println("[Proceso] Iniciando...");
        Scanner sc = new Scanner(System.in);
        int id=Integer.parseInt(args[0]);
        int n=Integer.parseInt(args[1]);
        int initialDelay=Integer.parseInt(args[2]);
        int bearer=Integer.parseInt(args[3]);
        //System.out.println("id: "+id+";  n: "+n+ ";  Delay: "+initialDelay+";  Bearer:"+bearer);
        if (System.getSecurityManager() == null)
            System.setSecurityManager(new SecurityManager());

        processes = new ArrayList<Inter>();
        String url;
        url = "rmi://localhost/process"+id;

            try{
                Inter process = (Inter) Naming.lookup(url);
                process.reset();
                processes.add(process);

            } catch (RemoteException e1){
                e1.printStackTrace();
            } catch (NotBoundException e2){
                e2.printStackTrace();
            } catch (MalformedURLException e3){
                e3.printStackTrace();
            }

        

        Inter process1 = getProcesses().get(0);
        TestThread thread1 = new TestThread(process1,initialDelay);

        try{
            process1.reset();
            int numero_verificador;
            System.out.println("[Proceso] Esperando a ejecutar su SC...");
            numero_verificador = leer();
            numero_verificador = numero_verificador-1;
            writeFile2(numero_verificador);
            while(numero_verificador != 0){ 
                TimeUnit.SECONDS.sleep(1);
                numero_verificador = leer();
            }
            new Thread(thread1).start();
            Token token = Token.instantiate(n);
            if (token != null && bearer==1){
                TokenMessage tm = new TokenMessage("",id,token);
                process1.takeToken(tm);
            };
            Thread.sleep(10000);


            /*
            Assert.assertTrue(process1.isComputationFinished());
            Assert.assertTrue(process2.isComputationFinished());
            Assert.assertTrue(process3.isComputationFinished());
            */
        } catch (Exception e){
            e.printStackTrace();
            //Assert.fail();
        }
    }

    static public List<Inter> getProcesses() {
        return processes;
    }

    public String[] getUrls() {
        return urls;
    }
    static public int leer() throws FileNotFoundException, IOException {
      File archivo;
      FileReader fr;
      BufferedReader br;
      int number = 20;
      archivo = new File ("out.txt");
      fr = new FileReader (archivo);
      br = new BufferedReader(fr);
      String linea;
      linea=br.readLine();
      number = Integer.parseInt(linea);  
      fr.close();          
      return number;
   }
    
   public static void writeFile2(int numero) throws IOException {
        FileWriter fw = new FileWriter("out.txt");

        fw.write(""+numero);
        

        fw.close();
    } 
    
}