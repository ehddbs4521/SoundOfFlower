package midas.SoundOfFlower.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import midas.SoundOfFlower.dto.response.DiaryInfoResponse;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Builder
@Table(name = "DIARY")
@AllArgsConstructor
public class Diary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1000)
    private String comment;

    private LocalDateTime date;

    private String flower;

    private Double angry;
    private Double sad;
    private Double delight;
    private Double calm;
    private Double embarrased;
    private Double anxiety;

    @ManyToOne
    @JoinColumn(name = "social_id", referencedColumnName = "socialId")
    private User user;

    @OneToOne
    @JoinColumn(name = "music_id")
    private Music music;

    public void setUser(User user) {

        if (this.user != null) {
            this.user.getDiary().remove(this);
        }
        this.user = user;

        if (user != null) {
            user.getDiary().add(this);
        }
    }

    public void updateComent(String comment) {
        this.comment = comment;
    }

    public void updateEmotion(Double angry, Double sad, Double delight, Double calm, Double embarrased, Double anxiety) {
        this.angry = angry;
        this.sad = sad;
        this.delight = delight;
        this.calm = calm;
        this.embarrased = embarrased;
        this.anxiety = anxiety;
    }

    public void updateFlower(String flower) {
        this.flower = flower;
    }

    public void updateMusicInfo(Music music) {
        this.music = music;
    }

    public DiaryInfoResponse toDiaryInfoResponse() {
        return DiaryInfoResponse.builder()
                .angry(this.angry)
                .sad(this.sad)
                .delight(this.delight)
                .calm(this.calm)
                .embarrased(this.embarrased)
                .anxiety(this.anxiety)
                .flower(this.flower)
                .musicId(this.music != null ? this.music.getMusicId() : null)
                .title(this.music != null ? this.music.getTitle() : null)
                .singer(this.music != null ? this.music.getSinger() : null)
                .likes(this.music != null ? this.music.getLikes() : null)
                .build();
    }
}
