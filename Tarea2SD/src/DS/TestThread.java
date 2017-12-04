

import java.rmi.RemoteException;

public class TestThread implements Runnable{

    private Inter process;
    private int delay_inicial;
    public TestThread(Inter process, int delay){
        this.process = process;
        this.delay_inicial=delay;
    }
    
    @Override
    public void run() {
        try{
            process.compute(delay_inicial);
        } catch (RemoteException e){
            throw new RuntimeException(e);
        }
    }
}
