package midas.SoundOfFlower.repository.diary;

import midas.SoundOfFlower.entity.Diary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long>, SearchDiary ,DeleteDiary{

}
