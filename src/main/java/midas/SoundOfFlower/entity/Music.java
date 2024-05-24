package midas.SoundOfFlower.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@Entity
@Builder
@Table(name = "MUSIC")
@AllArgsConstructor
public class Music {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "music_id")
    private Long id;

    @Column(name = "spotify", unique = true, nullable = false)
    private String spotify;


    private Double angry;
    private Double sad;
    private Double delight;
    private Double calm;
    private Double embarrased;
    private Double anxiety;
    private Double love;

    private Double totalLikes;

    @OneToMany(mappedBy = "music")
    private List<Diary> diary = new ArrayList<>();

    @OneToMany(mappedBy = "music", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MusicLike> musicLikes = new ArrayList<>();

    public void setTotalLikes(Double totalLikes) {
        this.totalLikes = totalLikes;
    }
}
