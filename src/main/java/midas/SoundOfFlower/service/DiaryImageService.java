package midas.SoundOfFlower.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import midas.SoundOfFlower.entity.DiaryImage;
import midas.SoundOfFlower.error.CustomException;
import midas.SoundOfFlower.repository.diary.DiaryRepository;
import midas.SoundOfFlower.repository.diaryimage.DiaryImageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static midas.SoundOfFlower.error.ErrorCode.OVER_COUNT;
import static midas.SoundOfFlower.error.ErrorCode.OVER_SIZE;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiaryImageService {

    @Value("${image.max_count}")
    private int MAX_FILE_COUNT;

    @Value("${image.max_size}")
    private int MAX_FILE_SIZE;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final AmazonS3Client amazonS3Client;
    private final DiaryImageRepository diaryImageRepository;


    public List<String> uploadDiaryImages(List<MultipartFile> images) throws IOException {

        if (!checkImageCount(images)) {
            throw new CustomException(OVER_COUNT);
        }

        if (!checkImageSize(images)) {
            throw new CustomException(OVER_SIZE);
        }

        List<String> imageUrls = saveImages(images);

        return imageUrls;
    }

    @Transactional
    public List<String> saveImages(List<MultipartFile> images) throws IOException {

        List<String> imageUrls = new ArrayList<>();

        for (MultipartFile image:images) {

            String fileName = image.getOriginalFilename();
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(image.getContentType());
            metadata.setContentLength(image.getSize());
            amazonS3Client.putObject(bucket, fileName, image.getInputStream(), metadata);
            String url = amazonS3Client.getUrl(bucket, fileName).toString();

            imageUrls.add(url);

        }

        return imageUrls;

    }

    private boolean checkImageCount(List<MultipartFile> images) {

        return images.size() <= MAX_FILE_COUNT ? true : false;
    }

    private boolean checkImageSize(List<MultipartFile> images) {

        for (MultipartFile image : images) {
            if (image.getSize() > MAX_FILE_SIZE) {
                return false;
            }
        }
        return true;
    }

    @Transactional
    public List<String> updateImageUrls(List<DiaryImage> diaryImages, List<MultipartFile> images) throws IOException {

        if (!checkImageCount(images)) {
            throw new CustomException(OVER_COUNT);
        }

        if (!checkImageSize(images)) {
            throw new CustomException(OVER_SIZE);
        }
        diaryImageRepository.deleteDiaryImage(diaryImages);
        List<String> newUrls = saveImages(images);

        return newUrls;
    }
}
