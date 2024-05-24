package midas.SoundOfFlower.redis.repository;

import midas.SoundOfFlower.redis.entity.MusicTotalLikes;
import org.springframework.data.repository.CrudRepository;

public interface MusicTotalLikesRepository extends CrudRepository<MusicTotalLikes, String> {
}
