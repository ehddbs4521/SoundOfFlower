package midas.SoundOfFlower.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MusicInfoResponse {

    private String state;
    private String musicId;
    private Double totalLikes;
}
