/**
 * Created by Kathy-Feña on 21-10-16.
 */

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Inter extends Remote {
    // Métodos
    public void waitToken() throws RemoteException;
    public void takeToken(String token) throws RemoteException;
    public void kill() throws RemoteException;
    public void receiveToken(TokenMessage tm) throws RemoteException;
    public int getIndex() throws RemoteException;
    public void receiveRequest(RequestMessage requestMessage) throws RemoteException;
    public boolean isComputationFinished() throws RemoteException;
    public void reset() throws RemoteException;
    public void compute() throws RemoteException;

}