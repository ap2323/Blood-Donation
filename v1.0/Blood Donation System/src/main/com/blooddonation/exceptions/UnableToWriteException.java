package main.com.blooddonation.exceptions;

public class UnableToWriteException extends RuntimeException{
    public UnableToWriteException(String message){
        super(message);
    }
    public UnableToWriteException(String message, Throwable ie){
        super(message, ie);
    }
}
