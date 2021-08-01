package kr.my.files.exception;

import kr.my.files.errors.ErrorCode;

public class OwnerNotMeachedException extends BusinessException {
    public OwnerNotMeachedException(String message) {
        super(message,ErrorCode.INVALID_INPUT_VALUE);
    }
}
