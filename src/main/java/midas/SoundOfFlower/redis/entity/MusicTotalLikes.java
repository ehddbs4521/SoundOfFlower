package midas.SoundOfFlower.redis.entity;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("MusicTotalLikes")
public class MusicTotalLikes implements Serializable {

    @Id
    private String id;

    private Double totalLikes;

    public void updateTotalLikes(Double totalLikes) {
        this.totalLikes = totalLikes;
    }


}
