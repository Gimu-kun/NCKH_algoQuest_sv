package com.example.algoQuestSV.Service;

import com.example.algoQuestSV.Dto.Api.ApiResponseDto;
import com.example.algoQuestSV.Dto.Lesson.LessonCreationDto;
import com.example.algoQuestSV.Dto.Lesson.LessonSectionCreateDto;
import com.example.algoQuestSV.Dto.Lesson.RefRequestDto;
import com.example.algoQuestSV.Entity.*;
import com.example.algoQuestSV.Repository.*;
import com.example.algoQuestSV.Utils.UploadUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
public class LessonService {
    @Autowired
    LessonRepository lessonRepository;

    @Autowired
    LessonSectionRepository sectionRepository;

    @Autowired
    LessonImgsRepository lessonImgsRepository;

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    UploadUtils uploadUtils;

    @Autowired
    TopicsRepository topicsRepository;

    // Bước 1: Tạo bài học trống (chỉ có tiêu đề)
    @Transactional
    public ApiResponseDto<Lesson> create(LessonCreationDto req) {
        User operator = usersRepository.findById(req.getOperatorId())
                .orElseThrow(() -> new RuntimeException("ID người thao tác không hợp lệ!"));

        Topic topic = topicsRepository.findById(req.getTopic_id())
                .orElseThrow(() -> new RuntimeException("ID chương không hợp lệ!"));

        Lesson lesson = new Lesson();
        lesson.setTitle(req.getTitle());
        lesson.setTopic(topic);
        lesson.setCreatedBy(operator);
        lesson.setUpdatedBy(operator);

        return ApiResponseDto.<Lesson>builder()
                .status(200)
                .message("Tạo bài học thành công")
                .data(lessonRepository.save(lesson))
                .build();
    }

    @Transactional
    public LessonSection addSection(
            String lessonId,
            String parentId,
            LessonSectionCreateDto dto,
            MultipartFile[] images
    ) throws IOException {

        LessonSection section = new LessonSection();
        section.setLessonId(lessonId);
        section.setTitle(dto.getTitle());
        section.setContent(dto.getContent());
        section.setLevel(dto.getLevel());

        if (parentId != null) {
            LessonSection parent = sectionRepository.findById(parentId)
                    .orElseThrow(() -> new RuntimeException("Parent section not found"));
            section.setParent(parent);
        }

        int orderIndex = (parentId == null)
                ? sectionRepository.countByLessonIdAndParentIsNull(lessonId)
                : sectionRepository.countByParentId(parentId);

        section.setOrderIndex(orderIndex + 1);
        System.out.println(dto.toString());
        // 🔹 XỬ LÝ REF
        if (dto.getRefs() != null) {
            for (RefRequestDto refDto : dto.getRefs()) {
                Refs ref = new Refs();
                ref.setType(refDto.getType());
                ref.setUrl(refDto.getUrl());
                ref.setSection(section);
                section.getRefs().add(ref);
            }
        }

        LessonSection savedSection = sectionRepository.save(section);

        // 🔹 XỬ LÝ IMAGE
        if (images != null) {
            for (int i = 0; i < images.length; i++) {
                String url = uploadUtils.uploadAvatar(
                        images[i],
                        "sec_" + savedSection.getId() + "_" + i
                );

                LessonImgs img = new LessonImgs();
                img.setSectionId(savedSection.getId());
                img.setUrl(url);
                img.setIndexOrder(i + 1);

                lessonImgsRepository.save(img);
            }
        }

        return savedSection;
    }

    public ApiResponseDto<Lesson> getById(String id) {
        return lessonRepository.findById(id)
                .map(lesson -> {
                    // QUAN TRỌNG: Lọc chỉ lấy các mục gốc (H1)
                    // Nhờ quan hệ @OneToMany(children), các mục H2 sẽ nằm gọn trong H1
                    List<LessonSection> rootSections = lessonRepository.findRootSections(id);
                    lesson.setSections(rootSections);

                    return ApiResponseDto.<Lesson>builder()
                            .status(200)
                            .message("Lấy thông tin bài học thành công!")
                            .data(lesson).build();
                })
                .orElse(ApiResponseDto.<Lesson>builder()
                        .status(404)
                        .message("Không tìm thấy bài học!").build());
    }

    public ApiResponseDto<List<Lesson>> getAll() {
        List<Lesson> lessons = lessonRepository.findAll();

        // Lọc cấu trúc cây cho từng bài học trong danh sách
        lessons.forEach(lesson -> {
            List<LessonSection> rootSections = lessonRepository.findRootSections(lesson.getId());
            lesson.setSections(rootSections);
        });

        return ApiResponseDto.<List<Lesson>>builder()
                .status(200)
                .data(lessons).build();
    }

    @Transactional
    public void deleteSection(String sectionId) {
        // 1. Xóa tất cả ảnh trong DB thuộc về section này trước
        lessonImgsRepository.deleteBySectionId(sectionId);

        // 2. Sau đó mới xóa section
        sectionRepository.deleteById(sectionId);
    }

    @Transactional
    public LessonSection updateSection(
            String id,
            LessonSection data,
            MultipartFile[] images
    ) throws IOException {

        // 1. Lấy Section hiện tại
        LessonSection section = sectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mục"));

        // 2. Update nội dung text
        section.setTitle(data.getTitle());
        section.setContent(data.getContent());

        // =====================================================
        // 3. UPDATE IMAGES (thay thế toàn bộ nếu có ảnh mới)
        // =====================================================
        if (images != null && images.length > 0) {

            if (section.getImages() == null) {
                section.setImages(new ArrayList<>());
            } else {
                section.getImages().clear(); // orphanRemoval = true
            }

            for (int i = 0; i < images.length; i++) {
                String url = uploadUtils.uploadAvatar(
                        images[i],
                        "sec_upd_" + id + "_" + i
                );

                LessonImgs img = new LessonImgs();
                img.setSectionId(id); // hoặc img.setLessonSection(section)
                img.setUrl(url);
                img.setIndexOrder(i + 1);

                section.getImages().add(img);
            }
        }

        // =====================================================
        // 4. UPDATE REFS (VIDEO / DOCUMENT)
        // =====================================================
        if (data.getRefs() != null) {

            if (section.getRefs() == null) {
                section.setRefs(new ArrayList<>());
            } else {
                section.getRefs().clear(); // xóa refs cũ
            }

            for (Refs refData : data.getRefs()) {

                Refs ref = new Refs();
                ref.setType(refData.getType());
                ref.setUrl(refData.getUrl());
                ref.setSection(section);

                section.getRefs().add(ref);
            }
        }

        // 5. Save (Hibernate tự xử lý cascade)
        return sectionRepository.save(section);
    }
}