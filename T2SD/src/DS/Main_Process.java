
import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.rmi.Naming;
import java.util.ArrayList;
import java.util.List;

public class Main_Process {

    private ArrayList<Inter> processes;

    void startServer() {
        int processIndex = 0;
        String[] urls = new String[3];
        urls[0] = "rmi://localhost/process1";
        urls[1] = "rmi://localhost/process2";
        urls[2] = "rmi://localhost/process3";
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
}


