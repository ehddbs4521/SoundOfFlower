package midas.SoundOfFlower.repository.music;

import midas.SoundOfFlower.entity.Music;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MusicRepository extends JpaRepository<Music, String> {

    Optional<Music> findBySpotify(String spotify);
}
