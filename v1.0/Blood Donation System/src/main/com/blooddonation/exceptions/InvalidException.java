package main.com.blooddonation.exceptions;

public class InvalidException extends RuntimeException{
    public InvalidException(String message){
        super(message);
    }
}
