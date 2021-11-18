package kr.my.files.exception;

import kr.my.files.errors.ErrorCode;

public class OverImagePixelException extends BusinessException {
    public OverImagePixelException(String message) {
        super(message, ErrorCode.INVALID_INPUT_VALUE);
    }
}