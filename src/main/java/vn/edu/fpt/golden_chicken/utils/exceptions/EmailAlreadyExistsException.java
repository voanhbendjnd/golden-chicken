package vn.edu.fpt.golden_chicken.utils.exceptions;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String value) {
        super("Email với " + value + " đã tồn tại!");
    }
}
