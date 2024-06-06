package main.com.blooddonation.exceptions;

import java.io.IOException;

public class UnableToReadException extends RuntimeException{
    public UnableToReadException(String message){
        super(message);
    }
    public UnableToReadException(String message, IOException ie){
        super(message, ie);
    }
}
