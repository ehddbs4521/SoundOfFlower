package midas.SoundOfFlower.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

@Slf4j
@Getter
@NoArgsConstructor
@Entity
@Builder
@Table(name = "DIARY_IMAGE")
@AllArgsConstructor
public class DiaryImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diary_image_id")
    private Long id;

    private String url;

    @ManyToOne
    @JoinColumn(name = "diary_id")
    private Diary diary;

    public void setDiary(Diary diary) {
        if (this.diary != null) {
            this.diary.getImageUrls().remove(this);
        }
        this.diary = diary;
        if (diary != null && diary.getImageUrls() != null) {
            diary.getImageUrls().add(this);
        } else if (diary != null) {
            diary.setImageUrls(new ArrayList<>());
            diary.getImageUrls().add(this);
        }
    }
}
