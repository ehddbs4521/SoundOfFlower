package midas.SoundOfFlower.redis.entity;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;

@Getter
@AllArgsConstructor
@RedisHash
public class BlackList implements Serializable {

    @Id
    private String socialId;

    @Indexed
    private String accessToken;

    @TimeToLive
    private Long expiration;
}
