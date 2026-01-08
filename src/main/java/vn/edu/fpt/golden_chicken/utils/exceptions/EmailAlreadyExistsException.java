package vn.edu.fpt.golden_chicken.utils.exceptions;

public class EmailAlreadyExistsException extends RuntimeException{
    public EmailAlreadyExistsException(String value) {
        super("Email with " + value + " already exists!");
    }
}
