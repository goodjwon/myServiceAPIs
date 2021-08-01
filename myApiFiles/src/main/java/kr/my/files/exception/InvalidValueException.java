package kr.my.files.exception;

import kr.my.files.errors.ErrorCode;

/**
 * 요청이 잘못 될 경우 발생
 */
public class InvalidValueException extends BusinessException {
    public InvalidValueException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
