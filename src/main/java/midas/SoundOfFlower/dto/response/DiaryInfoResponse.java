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

    private String flower;

    private List<String> imgUrl;

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
    private Double disLikes;

    public void updateFlower(String flower) {
        this.flower = flower;
    }

    public void updateImgUrl(List<String> imgUrl) {
        this.imgUrl = imgUrl;
    }

    public void updateEmotion(Double angry, Double sad, Double delight, Double calm, Double embarrased, Double anxiety) {
        this.angry = angry;
        this.sad = sad;
        this.delight = delight;
        this.calm = calm;
        this.embarrased = embarrased;
        this.anxiety = anxiety;
    }

    public void updateMusicDetails(Long musicId, String title, String singer, Double likes, Double disLikes) {
        this.musicId = musicId;
        this.title = title;
        this.singer = singer;
        this.likes = likes;
        this.disLikes = disLikes;
    }
}
