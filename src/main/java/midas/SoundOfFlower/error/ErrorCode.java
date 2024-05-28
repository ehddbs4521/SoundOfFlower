package midas.SoundOfFlower.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorCode {

    EXIST_USER_EMAIL_SOCIALTYPE(HttpStatus.CONFLICT, "SAU1","해당 socialType의 이메일이 이미 존재합니다."),
    EXIST_USER_NICKNAME(HttpStatus.CONFLICT, "SAU2","해당 닉네임이 이미 존재합니다."),
    NOT_EXIST_USER_EMAIL_SOCIALTYPE(HttpStatus.NOT_FOUND, "SAU3","해당 socialType의 이메일이 존재하지 않습니다."),
    NOT_EXIST_USER_EMAIL(HttpStatus.NOT_FOUND, "SAU4","이메일이 존재하지않습니다."),
    NOT_EXIST_USER_NICKNAME(HttpStatus.NOT_FOUND,"SAU5", "닉네임이 존재하지않습니다."),
    NOT_EXIST_USER_SOCIALID(HttpStatus.NOT_FOUND, "SAU6","socialId가 존재하지않습니다."),
    EXIST_ACCESSTOKEN_BLACKLIST(HttpStatus.CONFLICT,"SAT1","이미 로그아웃한 사용자입니다."),
    NOT_EXIST_REFRESHTOKEN(HttpStatus.NOT_FOUND,"SAT2","존재하지 않는 Refresh Token입니다."),
    NOT_VALID_REFRESHTOKEN(HttpStatus.BAD_REQUEST,"SAT3","유효하지 않은 Refresh토큰입니다."),
    NOT_EXTRACT_SOCIALID(HttpStatus.UNAUTHORIZED, "SAT4","토큰에서 socialId를 추출 할 수 없습니다.(잘못된 토큰)"),
    NOT_VALID_ACCESSTOKEN(HttpStatus.UNAUTHORIZED, "SAT5","Access토큰이 유효하지 않습니다."),
    WRONG_SIGNATURE_TOKEN(HttpStatus.UNAUTHORIZED, "SAT6","잘못된 JWT 서명입니다."),
    NOT_EQUAL_JSON(HttpStatus.BAD_REQUEST, "SAD1","data content-type이 json이 아닙니다."),
    NOT_EXTRACT_ACCESSTOKEN(HttpStatus.UNAUTHORIZED, "SAT7","토큰에서 AccessToken을 추출 할 수 없습니다.(잘못된 토큰)"),
    WRONG_CERTIFISATION_NUMBER(HttpStatus.BAD_REQUEST,"SAC1","인증번호가 틀렸습니다."),
    EXPIRE_CERTIFISATION_NUMBER(HttpStatus.BAD_REQUEST,"SAC2","인증번호가 만료되었습니다."),
    EXPIRE_TOKEN(HttpStatus.BAD_REQUEST,"SAT8","토큰이 만료되었습니다."),
    NOT_SURPPORTED_TOKEN(HttpStatus.BAD_REQUEST,"SAT9","지원되지 않는 JWT 토큰입니다."),
    WRONG_PASSWORD(HttpStatus.BAD_REQUEST,"SAC3","비밀번호를 재입력해주세요"),
    NOT_SoundOfFlower_SOCIALTYPE(HttpStatus.BAD_REQUEST,"SAC4","자체 서비스 회원가입 시 만든 비밀번호만 변경 가능합니다."),
    SEND_EMAIL_FAIL(HttpStatus.INTERNAL_SERVER_ERROR,"SAS1","이메일 전송을 실패했습니다."),
    NOT_EQUAL_EACH_TOKEN_SOCIALID(HttpStatus.BAD_REQUEST, "SAT10","로그인한 사용자의 Refresh Token이 아닙니다"),
    ILLEGAL_TOKEN(HttpStatus.BAD_REQUEST, "SAT11","JWT 토큰이 잘못되었습니다."),

    WRONG_ADMIN_CODE(HttpStatus.BAD_REQUEST,"SAA1","Admin코드가 틀렸습니다."),
    EXIST_ADMIN_EMAIL(HttpStatus.BAD_REQUEST,"SAA2","Admin 이메일이 존재합니다."),

    NOT_EXIST_MUSIC_SPOTIFY(HttpStatus.NOT_FOUND, "SAM1", "해당 spotify를 찾을 수 없습니다."),
    EXTERNAL_API_FAILURE(HttpStatus.BAD_GATEWAY,"SAG1","외부 api와 통신이 불가능합니다."),

    OVER_SIZE(HttpStatus.BAD_REQUEST,"SAI1","용량은 2MB를 초과 할 수 없습니다."),
    OVER_COUNT(HttpStatus.BAD_REQUEST,"SAI2","갯수는 9개를 초과 할 수 없습니다."),

    NOT_EXIST_DIARY(HttpStatus.NOT_FOUND,"SAD1","일기를 찾을 수 없습니다."),
    NOT_EXIST_TITLE_DIARY(HttpStatus.NOT_FOUND,"SAD2","제목이 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}