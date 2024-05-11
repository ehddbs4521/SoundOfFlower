package midas.SoundOfFlower.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Builder
@Table(name = "DIARY")
@AllArgsConstructor
public class Diary {

    @Id
    private Long id;

    @Column(length = 1000)
    private String comment;

    @Temporal(TemporalType.DATE)
    private LocalDateTime date;

    private String flower;

    private Double angry;
    private Double sad;
    private Double delight;
    private Double calm;
    private Double embarrased;
    private Double anxiety;

    @ManyToOne
    @JoinColumn(name = "nick_name")
    private User user;

    @OneToOne
    @JoinColumn(name = "music_id")
    private Music music;

    public void setUser(User user) {

        if (this.user != null) {
            this.user.getDiary().remove(this);
        }
        this.user = user;
    }

}
