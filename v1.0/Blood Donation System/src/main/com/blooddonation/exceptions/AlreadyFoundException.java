package main.com.blooddonation.exceptions;

public class AlreadyFoundException extends RuntimeException{
    public AlreadyFoundException(String message){
        super(message);
    }

    public AlreadyFoundException(String message, RuntimeException re){
        super(message, re);
    }
}
