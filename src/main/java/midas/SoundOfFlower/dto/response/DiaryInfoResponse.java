package midas.SoundOfFlower.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiaryInfoResponse {

    private Long diaryId;

    private String flower;

    private List<String> imgUrl;

    private Double angry;
    private Double sad;
    private Double delight;
    private Double calm;
    private Double embarrased;
    private Double anxiety;
    private Double love;

    private String spotify;

    private boolean isLike;

    public void updateFlower(String flower) {
        this.flower = flower;
    }

    public void updateImgUrl(List<String> imgUrl) {
        this.imgUrl = imgUrl;
    }

    public void updateEmotion(Double angry, Double sad, Double delight, Double calm, Double embarrased, Double anxiety,Double love) {
        this.angry = angry;
        this.sad = sad;
        this.delight = delight;
        this.calm = calm;
        this.embarrased = embarrased;
        this.anxiety = anxiety;
        this.love = love;
    }

    public void updateMusic(String spotify) {
        this.spotify = spotify;
    }

    public void updateLike(boolean isLike) {
        this.isLike = isLike;
    }

    public void setImgUrl(List<String> imgUrl) {
        this.imgUrl = imgUrl;
    }
}
