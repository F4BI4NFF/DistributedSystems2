

import java.rmi.RemoteException;

public class TestThread implements Runnable{

    private Inter process;
    
    public TestThread(Inter process){
        this.process = process;
    }
    
    @Override
    public void run() {
        try{
            process.compute();
        } catch (RemoteException e){
            throw new RuntimeException(e);
        }
    }
}
