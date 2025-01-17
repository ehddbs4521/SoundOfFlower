package midas.SoundOfFlower.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserRequest {

    @Email
    @NotNull
    private String email;

    @NotNull
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&]).{10,15}$",
            message = "비밀번호는 문자, 숫자, 특수문자를 포함하여 10자리 이상 15자리 이하이어야 합니다.")
    private String password;

    @NotNull
    private String adminCode;
}
