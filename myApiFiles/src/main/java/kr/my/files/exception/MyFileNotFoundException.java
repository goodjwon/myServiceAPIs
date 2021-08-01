package kr.my.files.exception;

import kr.my.files.errors.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 파일을 찾지 못할 경우 활용.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class MyFileNotFoundException extends BusinessException {
    public MyFileNotFoundException(String message) {
        super(message, ErrorCode.INVALID_INPUT_VALUE);
    }
}