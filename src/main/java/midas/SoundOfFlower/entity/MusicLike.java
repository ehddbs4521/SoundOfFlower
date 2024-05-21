package midas.SoundOfFlower.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Builder
@Table(name = "MUSIC_LIKE")
@AllArgsConstructor
public class MusicLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "music_like_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "music_id", referencedColumnName = "music_id")
    private Music music;

    private Boolean isLike;

    public void setUser(User user) {
        if (this.user != null) {
            this.user.getMusicLikes().remove(this);
        }

        this.user = user;
        if (user != null) {
            user.getMusicLikes().add(this);
        }
    }

    public void setMusic(Music music) {
        if (this.music != null) {
            this.music.getMusicLikes().remove(this);
        }

        this.music = music;
        if (music != null) {
            music.getMusicLikes().add(this);
        }
    }


    public void updateLike(boolean isLike) {

        this.isLike = isLike;
    }
}
