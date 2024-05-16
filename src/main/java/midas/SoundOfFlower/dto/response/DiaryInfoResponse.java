package midas.SoundOfFlower.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiaryInfoResponse {

    private String flower;

    private Double angry;
    private Double sad;
    private Double delight;
    private Double calm;
    private Double embarrased;
    private Double anxiety;

    private Long musicId;
    private String title;
    private String singer;
    private Double likes;
}
