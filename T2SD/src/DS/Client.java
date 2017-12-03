import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Scanner;
import java.rmi.Naming;
import java.util.ArrayList;
import java.util.List;



public class Client {
    static private List<Inter> processes;
    static private String[] urls;
    public static void main(String[] args) throws RemoteException {

        System.out.println("[Cliente] Iniciando...");

        if (System.getSecurityManager() == null)
            System.setSecurityManager(new SecurityManager());

        processes = new ArrayList<Inter>();
        String[] urls = new String[3];
        urls[0] = "rmi://localhost/process1";
        urls[1] = "rmi://localhost/process2";
        urls[2] = "rmi://localhost/process3";

        for (String url : urls){
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

        }

        Inter process1 = getProcesses().get(0);
        TestThread thread1 = new TestThread(process1);
        Inter process2 = getProcesses().get(1);
        TestThread thread2 = new TestThread(process2);
        Inter process3 = getProcesses().get(2);
        TestThread thread3 = new TestThread(process3);

        try{
            process1.reset();
            process2.reset();
            process3.reset();
            new Thread(thread1).start();
            Token token = Token.instantiate(3);
            if (token != null){
                TokenMessage tm = new TokenMessage("",0,token);
                process1.receiveToken(tm);
            }
            new Thread(thread2).start();
            new Thread(thread3).start();

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
}