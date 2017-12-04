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
    private String color = null;
    private int cantidad_de_token = 0;
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
        color = "Amarillo";
        inCriticalSection = false;
        computationFinished = false;

    }

    public void compute(int delay) throws RemoteException {
        long t1 = System.currentTimeMillis();
        System.out.println("El proceso " + index + " ha entrado a al conjunto de procesos.");
        try {
            Thread.sleep(random.nextInt(MAX_COMPUTATION_DELAY));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        criticalSectionWrapper(delay);

        try {
            Thread.sleep(random.nextInt(MAX_COMPUTATION_DELAY));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
            
        long t2 = System.currentTimeMillis();
        computationFinished = true;
        System.out.println("El proceso " + index + " ha demorado " + (t2 - t1) + " ms. en ejecutar su SC.");
    }

    private void criticalSectionWrapper(int delay) {
        Request();
        waitToken();
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        criticalSection();
        dispatchToken();
    }

    private void Request() {
        N.set(index, N.get(index) + 1);
        System.out.println("[Proceso '" + index +"'," +color+ "] Realiza Request a otros procesos.");
        System.out.println("[Proceso '" + index +"'," +color+ "] RN"+index+": " + N);
        
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
        System.out.println("[Proceso '" + index +"'," +color+ "] Recibe Request de proceso " + rm.getSrcId() + " con numero de secuencia " + rm.getSequence());
        N.set(rm.getSrcId(), rm.getSequence());
        System.out.println("[Proceso '" + index +"'," +color+ "] RN"+index+": " + N);

        if (!inCriticalSection && token != null && index != rm.getSrcId() &&
                (N.get(rm.getSrcId()) > token.getTN().get(rm.getSrcId()))) {
            sendToken(rm.getSrcUrl());
        }
    }

    private void sendToken(String url) {
        assert token != null;
        Inter dest = getProcess(url);

        try {
            System.out.println("[Proceso '" + index +"'," +color+ "] Envia token a Proceso " + dest.getIndex());
            TokenMessage tm = new TokenMessage(urls[index], index, token);
            dest.takeToken(tm);
            token = null;
            color = "Verde";
            if(cantidad_de_token == 1){color = "Amarillo";}
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public void takeToken(TokenMessage tm) {
       token = tm.getToken();
       cantidad_de_token = cantidad_de_token+1;
       if (token != null && color == "Amarillo"){
           color="Verde";}
       System.out.println("[Proceso '" + index +"'," +color+ "] Recibe exitosamente el token");
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
                System.out.println("[Proceso '" + index +"'," +color+ "] Sigue en espera del token...");
            }
        }
    }

    private void criticalSection() {
        inCriticalSection = true;
        color="Rojo";
        int tiempo_SC = random.nextInt(MAX_COMPUTATION_DELAY);
        System.out.println("[Proceso '" + index +"'," +color+ "] Entra a su SC y la ejecutara en " + tiempo_SC + " ms.");
        cantidad_de_token = cantidad_de_token+1;
        try {
            Thread.sleep(tiempo_SC);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        color="Verde";
        System.out.println("[Proceso '" + index +"'," +color+ "] Ejecuto su SC de manera exitosa.");
        inCriticalSection = false;
    }

    private void dispatchToken() {
        token.getTN().set(index, N.get(index));
        System.out.println("[Proceso '" + index +"'," +color+ "] Intenta enviar token, va a verificar condiciones...");
        System.out.println("TN: " + token.getTN());
        System.out.println("N: " + N);

        for (int j = 0; j < numProcesses && token != null; j++) {
            if (j == index) {
                continue;
            }
            if (N.get(j) > token.getTN().get(j)) {
                sendToken(urls[j]);
                System.out.println("[Proceso '" + index +"'," +color+ "] Ha enviado el token al Proceso " + j);
                break;
            }
        }
    }

    @Override
    public boolean isComputationFinished() {
        return computationFinished;
    }
    public void kill() throws RemoteException{
        System.out.println("Este metodo detiene el algoritmo y mata un proceso!");
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
