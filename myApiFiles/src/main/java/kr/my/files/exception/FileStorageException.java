package kr.my.files.exception;

import kr.my.files.errors.ErrorCode;

/**
 * 파일 저장 관련에러 발생시 사용.
 */
public class FileStorageException extends BusinessException {
    public FileStorageException(String message) {
        super(message, ErrorCode.INVALID_INPUT_VALUE);
    }
}