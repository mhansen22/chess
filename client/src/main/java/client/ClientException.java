package client;

public class ClientException extends Exception {
    private final int code;

    public ClientException(String message) {
        super(message);
        this.code = 0;
    }
    //w/ message
    public ClientException(int statusCode, String message) {
        super(message);
        this.code = statusCode;
    }
}