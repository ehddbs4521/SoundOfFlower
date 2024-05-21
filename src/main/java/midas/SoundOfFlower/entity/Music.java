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
    @Column(name = "music_id")
    private Long musicId;

    private Double angry;
    private Double sad;
    private Double delight;
    private Double calm;
    private Double embarrased;
    private Double anxiety;

    private Double totalLikes;

    @OneToMany(mappedBy = "music", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MusicLike> musicLikes = new ArrayList<>();

    public void setLikes(Double totalLikes) {
        this.totalLikes = totalLikes;
    }
}
