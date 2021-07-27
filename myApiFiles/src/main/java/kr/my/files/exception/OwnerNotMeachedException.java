package kr.my.files.exception;

public class OwnerNotMeachedException extends InvalidValueException {
    public OwnerNotMeachedException(String message) {
        super(ErrorCode.INVALID_INPUT_VALUE);
    }
}
