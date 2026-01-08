package vn.edu.fpt.golden_chicken.utils.exceptions;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String entity, String value) {
        super(entity + " with " + value + "not found!");
    }

}
