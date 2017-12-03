/**
 * Created by Kathy-Feña on 21-10-16.
 */

import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.*;
import java.io.*;
import java.util.*;
import java.io.Serializable;


public class Server extends UnicastRemoteObject implements Inter , Runnable {

    private int index;

    /**
     * Maximum delay simulating a computation unit within {@link #compute()} method and a critical section.
     */
    private static final int MAX_COMPUTATION_DELAY = 1000;

    /**
     * A delay between checks of token acquisition.
     */
    private static final int TOKEN_WAIT_DELAY = 10; // Modificar en base a cada proceso

    private Map<String, Inter> processCache;
    private List<Integer> N;
    private String[] urls;


    /**
     * Number of processes participating in message exchange
     */
    private int numProcesses;

    private Token token = null;
    private boolean inCriticalSection = false;


    /**
     * Needs to simulate random delays while doing computations
     */
    private Random random = new Random();

    /**
     * Is true after the computations are done, is needed for debug purposes
     */
    private boolean computationFinished = false;



    protected Server(String[] urls,int index) throws RemoteException{
        super();
        processCache = new HashMap<String,Inter>();
        this.index = index;
        this.urls = urls;
        this.numProcesses = urls.length;
        reset();
    }

    public void reset(){
        this.N = new ArrayList<Integer>(numProcesses);
        for (int i = 0; i < numProcesses; i++) {
            N.add(0);
        }

        token = null;
        inCriticalSection = false;
        computationFinished = false;

    }

    public void compute() throws RemoteException {
        long t1 = System.currentTimeMillis();
        System.out.println("Process " + index + " starts computations.");
        try {
            Thread.sleep(random.nextInt(MAX_COMPUTATION_DELAY));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        criticalSectionWrapper();

        try {
            Thread.sleep(random.nextInt(MAX_COMPUTATION_DELAY));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        long t2 = System.currentTimeMillis();
        computationFinished = true;
        System.out.println("Process " + index + " ends computations which lasted for " + (t2 - t1) + " ms.");
    }

    private void criticalSectionWrapper() {
        Request();
        waitToken();
        criticalSection();
        dispatchToken();
    }

    private void Request() {
        N.set(index, N.get(index) + 1);
        System.out.println("(" + index + ") broadcasting request");
        System.out.println("(" + index + ") N: " + N);
        for (String url : urls) {
            Inter dest = getProcess(url);
            try {
                RequestMessage rm = new RequestMessage(urls[index], index, N.get(index));
                dest.receiveRequest(rm);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Inter getProcess(String url) {
        Inter result = processCache.get(url);
        if (result == null) {
            try {
                result = (Inter) Naming.lookup(url);
            } catch (RemoteException e1) {
                throw new RuntimeException(e1);
            } catch (MalformedURLException e2) {
                throw new RuntimeException(e2);
            } catch (NotBoundException e3) {
                throw new RuntimeException(e3);
            }
            processCache.put(url, result);
        }
        return result;
    }

    public synchronized void receiveRequest(RequestMessage rm) {
        System.out.println("(" + index + ") received request from " + rm.getSrcId() + " with seq. " + rm.getSequence());
        N.set(rm.getSrcId(), rm.getSequence());
        System.out.println("(" + index + ") N: " + N);

        if (!inCriticalSection && token != null && index != rm.getSrcId() &&
                (N.get(rm.getSrcId()) > token.getTN().get(rm.getSrcId()))) {
            sendToken(rm.getSrcUrl());
        }
    }

    private void sendToken(String url) {
        assert token != null;
        Inter dest = getProcess(url);

        try {
            System.out.println("(" + index + ") sends token to " + dest.getIndex());
            TokenMessage tm = new TokenMessage(urls[index], index, token);
            dest.receiveToken(tm);
            token = null;
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public void receiveToken(TokenMessage tm) {
       System.out.println("(" + index + ") received token");
        token = tm.getToken();
    }

    public void waitToken() {
        int waitForToken = 0;
        while (token == null) {
            waitForToken++;
            try {
                Thread.sleep(TOKEN_WAIT_DELAY);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (waitForToken % 100 == 0) {
                System.out.println("(" + index + ") keeps waiting for token");
            }
        }
    }

    private void criticalSection() {
        inCriticalSection = true;
        int delay = random.nextInt(MAX_COMPUTATION_DELAY);
        // CAMBIAR CON EL DELAY QUE ES PASADO!
        System.out.println("Process " + index + " enters critical section and will compute for " + delay + " ms.");

        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Process " + index + " leaves critical section.");
        inCriticalSection = false;
    }

    private void dispatchToken() {
        token.getTN().set(index, N.get(index));
        System.out.println("(" + index + ") tries to dispatch token");
        System.out.println("TN: " + token.getTN());
        System.out.println("N: " + N);

        for (int j = 0; j < numProcesses && token != null; j++) {
            if (j == index) {
                continue;
            }
            if (N.get(j) > token.getTN().get(j)) {
                sendToken(urls[j]);
                System.out.println("(" + index + ") dispatched token to " + j);
                break;
            }
        }
    }

    @Override
    public boolean isComputationFinished() {
        return computationFinished;
    }



    public void takeToken(String token) throws RemoteException{
        System.out.println("Ahora posees el token, puedes ejecutar tu SC");

    }
    public void kill() throws RemoteException{
        System.out.println("Este método detiene el algoritmo y mata un proceso!");
    }


    public static void main(String[] args) {
        try {
            LocateRegistry.createRegistry(1099);
        }
        catch (RemoteException e){
            e.printStackTrace();
        }
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new RMISecurityManager());
        }

        System.out.println("[Servidor] Iniciando...");
        try {
            new Main_Process().startServer();
            System.out.println("[Servidor] Listo!");
        }
        catch (Exception e) {
            System.err.println("Excepcion en Servidor:");
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void run() {
        System.out.println("Runnable!");

    }
}
