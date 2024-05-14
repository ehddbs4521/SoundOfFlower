package midas.SoundOfFlower.repository;

import midas.SoundOfFlower.entity.Music;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MusicRepository extends JpaRepository<Music, Long> {

    Optional<Music> findByMusicId(Long musicId);
}
