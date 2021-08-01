package kr.my.files.errors;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 에러코드와 메세지 정의
 *
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ErrorCode {

    // Common
    INVALID_INPUT_VALUE(400, "C001", " Invalid Input Value"),
    METHOD_NOT_ALLOWED(405, "C002", " Invalid Input Value"),
    ENTITY_NOT_FOUND(400, "C003", " Entity Not Found"),
    INTERNAL_SERVER_ERROR(500, "C004", "Server Error"),
    INVALID_TYPE_VALUE(400, "C005", " Invalid Type Value"),
    HANDLE_ACCESS_DENIED(403, "C006", "Access is Denied"),


    // Users
    EMAIL_DUPLICATION(400, "M001", "이메일은 이미 사용중 입니다."),
    LOGIN_INPUT_INVALID(400, "M002", "로그인 필 수 값을 확인해 주세요"),
    DUPLICATE_USER_ID(400, "M003", "이미사용중인 아이디 입니다."),

    // KoreaCodes
    KOREA_CODE_ALREADY_USE(400, "CO001", "행정코드는 이미 사용중 입니다."),
    KOREA_CODE_EXPIRE(400, "CO002", "행정코드는 사용이 만료 되었습니다."),

    //Nesi Codes
    NEIS_CODE_ALREADY_USE(400, "NE0001", "나이스코드는 이미 사용중 입니다." ),
    NEIS_CODE_EXPIRE(400, "NE0001", "나이스코드는 사용이 만료 되었습니다." )

    ;
    private final String code;
    private final String message;
    private final int status;

    ErrorCode(final int status, final String code, final String message) {
        this.status = status;
        this.message = message;
        this.code = code;
    }

    public String getMessage() {
        return this.message;
    }

    public String getCode() {
        return code;
    }

    public int getStatus() {
        return status;
    }


}
