package midas.SoundOfFlower.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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

    @Column(length = 50)
    private String title;

    @Column(length = 1000)
    private String comment;

    @Column(unique = true)
    private LocalDate date;

    @Column(length = 10)
    private String flower;

    private Double angry;
    private Double sad;
    private Double delight;
    private Double calm;
    private Double embarrased;
    private Double anxiety;
    private Double love;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "social_id", referencedColumnName = "socialId")
    private User user;

    @ManyToOne
    @JoinColumn(name = "spotify", referencedColumnName = "spotify")
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

    public void setMusic(Music music) {
        if (this.music != null) {
            this.music.getDiary().remove(this);
        }

        this.music = music;
        if (music != null) {
            music.getDiary().add(this);
        }
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateComment(String comment) {
        this.comment = comment;
    }

    public void updateEmotion(Double angry, Double sad, Double delight, Double calm, Double embarrased, Double anxiety, Double love) {
        this.angry = angry;
        this.sad = sad;
        this.delight = delight;
        this.calm = calm;
        this.embarrased = embarrased;
        this.anxiety = anxiety;
        this.love = love;
    }

    public void updateFlower(String flower) {
        this.flower = flower;
    }

    public void setImageUrls(List<DiaryImage> imageUrls) {
        this.imageUrls = imageUrls;
    }
}
