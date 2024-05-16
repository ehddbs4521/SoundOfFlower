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
@Table(name = "MUSIC")
@AllArgsConstructor
public class Music {

    @Id
    private Long musicId;
    private String title;
    private String singer;

    private Double angry;
    private Double sad;
    private Double delight;
    private Double calm;
    private Double embarrased;
    private Double anxiety;

    private Double likes;

}
