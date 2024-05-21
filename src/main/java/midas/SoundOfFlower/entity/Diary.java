package midas.SoundOfFlower.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import midas.SoundOfFlower.dto.response.DiaryInfoResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@Entity
@Builder
@Table(name = "DIARY")
@AllArgsConstructor
public class Diary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diary_id")
    private Long id;

    private String title;

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

    private Boolean musicLike;
    private Boolean musicDisLike;

    @ManyToOne
    @JoinColumn(name = "social_id", referencedColumnName = "socialId")
    private User user;

    @OneToOne
    @JoinColumn(name = "music_id")
    private Music music;

    @OneToMany(mappedBy = "diary", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DiaryImage> imageUrls = new ArrayList<>();

    public void setUser(User user) {
        if (this.user != null) {
            this.user.getDiary().remove(this);
        }

        this.user = user;
        if (user != null) {
            user.getDiary().add(this);
        }
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateComment(String comment) {
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

    public void setImageUrls(List<DiaryImage> imageUrls) {
        this.imageUrls = imageUrls;
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
                .musicId(this.music.getMusicId())
                .totalLikes(this.music.getTotalLikes())
                .build();
    }
}
