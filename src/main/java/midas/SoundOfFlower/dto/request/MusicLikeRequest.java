package midas.SoundOfFlower.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MusicLikeRequest {

    private String spotify;
    private boolean like;
}
