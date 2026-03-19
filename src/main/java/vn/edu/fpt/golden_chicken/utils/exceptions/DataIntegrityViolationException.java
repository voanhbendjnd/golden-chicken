package vn.edu.fpt.golden_chicken.utils.exceptions;

public class DataIntegrityViolationException extends RuntimeException {
    public DataIntegrityViolationException(Object object) {
        super(object + "");
    }
}
