

public class TokenMessage extends Message {

    private Token token;

    public TokenMessage(String srcUrl, int srcId, Token token){
        super(srcUrl, srcId);
        this.token = token;
    }

    public Token getToken() {
        return token;
    }
}
