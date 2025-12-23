package com.example.algoQuestSV.Service;

import com.example.algoQuestSV.Dto.Api.ApiResponseDto;
import com.example.algoQuestSV.Dto.Lesson.LessonCreationDto;
import com.example.algoQuestSV.Entity.Lesson;
import com.example.algoQuestSV.Entity.LessonImgs;
import com.example.algoQuestSV.Entity.Quest;
import com.example.algoQuestSV.Entity.User;
import com.example.algoQuestSV.Repository.LessonImgsRepository;
import com.example.algoQuestSV.Repository.LessonRepository;
import com.example.algoQuestSV.Repository.UsersRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LessonService {
    @Autowired
    LessonRepository lessonRepository;

    @Autowired
    LessonImgsRepository lessonImgsRepository;

    @Autowired
    UsersRepository usersRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    private List<String> saveFiles(MultipartFile[] files) throws IOException {
        List<String> filePaths = new ArrayList<>();

        if (files == null || files.length == 0) {
            return filePaths;
        }

        File directory = new File(uploadDir);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Could not create upload directory");
        }

        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                String originalName = Paths.get(file.getOriginalFilename()).getFileName().toString();
                String fileName = UUID.randomUUID() + "_" + originalName;

                Path copyLocation = Paths.get(uploadDir, fileName);
                Files.copy(file.getInputStream(), copyLocation, StandardCopyOption.REPLACE_EXISTING);

                filePaths.add("/uploads/" + fileName);
            }
        }
        return filePaths;
    }

    @Transactional
    public ApiResponseDto<Lesson> create(LessonCreationDto req) {
        try {
            Optional<User> optUser = usersRepository.findById(req.getOperatorId());

            if (optUser.isEmpty()){
                return ApiResponseDto.<Lesson>builder()
                        .status(400)
                        .message("ID người thao tác không hợp lệ!")
                        .data(null)
                        .build();
            }

            User operator = optUser.get();

            Lesson lesson = new Lesson();
            lesson.setTitle(req.getTitle());
            lesson.setContent(req.getContent());
            lesson.setCreatedBy(operator);
            lesson.setUpdatedBy(operator);
            lessonRepository.save(lesson);

            List<String> urls = saveFiles(
                    req.getImages() != null
                            ? req.getImages().toArray(new MultipartFile[0])
                            : new MultipartFile[0]
            );

            List<LessonImgs> lessonImgsList = new ArrayList<>();
            for (int i = 0; i < urls.size(); i++) {
                LessonImgs lessonImgs = new LessonImgs();
                lessonImgs.setLessonId(lesson.getId());
                lessonImgs.setUrl(urls.get(i));
                lessonImgs.setIndexOrder(i + 1);
                lessonImgsList.add(lessonImgs);
            }

            if (!lessonImgsList.isEmpty()) {
                lessonImgsRepository.saveAll(lessonImgsList);
            }

            return ApiResponseDto.<Lesson>builder()
                    .status(200)
                    .message("Tạo bài học thành công")
                    .data(lesson)
                    .build();

        } catch (Exception ex) {
            throw new RuntimeException("Lesson creation failed", ex);
        }
    }

    public ApiResponseDto<List<Lesson>> getAll() {
        return ApiResponseDto.<List<Lesson>>builder()
                .status(200)
                .message("Lấy danh sách bài học thành công!")
                .data(lessonRepository.findAll())
                .build();
    }

    public ApiResponseDto<Lesson> getById(String id) {
        Optional<Lesson> optionalLesson = lessonRepository.findById(id);
        if (optionalLesson.isPresent()){
            return ApiResponseDto.<Lesson>builder()
                    .status(200)
                    .message("Lấy thông tin bài học thành công!")
                    .data(optionalLesson.get())
                    .build();
        }
        return ApiResponseDto.<Lesson>builder()
                .status(404)
                .message("Không tìm thấy thông tin bài học!")
                .data(null)
                .build();
    }
}
