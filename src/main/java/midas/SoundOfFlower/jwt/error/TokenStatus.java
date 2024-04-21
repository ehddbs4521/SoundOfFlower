package midas.SoundOfFlower.jwt.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TokenStatus {

    SUCCESS("옳바른 JWT 토큰입니다."),
    WRONG_SIGNATURE("잘못된 JWT 서명입니다."),
    EXPIRED("만료된 JWT 토큰입니다."),
    UNSUPPORTED("지원되지 않는 JWT 토큰입니다."),
    ILLEGAL_TOKEN("JWT 토큰이 잘못되었습니다.")
    ;

    private final String error;
}
