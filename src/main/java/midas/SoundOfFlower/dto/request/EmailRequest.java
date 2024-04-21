package midas.SoundOfFlower.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {

    private String email;
    private String emailType;
    private String socialType;

}
