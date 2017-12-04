

public class RequestMessage extends Message {

    private int sequence;
    
    public RequestMessage(String srcUrl, int srcId, int sequence){
        super(srcUrl, srcId);
        this.sequence = sequence;
    }

    public int getSequence() {
        return sequence;
    }
}
