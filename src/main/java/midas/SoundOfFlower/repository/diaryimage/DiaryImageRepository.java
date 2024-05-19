package midas.SoundOfFlower.repository.diaryimage;

import midas.SoundOfFlower.entity.DiaryImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiaryImageRepository extends JpaRepository<DiaryImage, Long>, DeleteDiaryImage {


}
