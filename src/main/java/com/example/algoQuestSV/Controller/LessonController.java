package com.example.algoQuestSV.Controller;

import com.example.algoQuestSV.Dto.Api.ApiResponseDto;
import com.example.algoQuestSV.Dto.Lesson.LessonCreationDto;
import com.example.algoQuestSV.Dto.Lesson.LessonSectionCreateDto;
import com.example.algoQuestSV.Dto.Lesson.LessonSectionResponseDto;
import com.example.algoQuestSV.Entity.Lesson;
import com.example.algoQuestSV.Entity.LessonSection;
import com.example.algoQuestSV.Repository.LessonRepository;
import com.example.algoQuestSV.Repository.LessonSectionRepository;
import com.example.algoQuestSV.Service.LessonService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("api/lessons")
@CrossOrigin("*")
public class LessonController {
    @Autowired
    LessonService lessonService;

    @Autowired
    LessonRepository lessonRepository;

    @Autowired
    LessonSectionRepository lessonSectionRepository;

    @GetMapping
    public ApiResponseDto<List<Lesson>> getAll() {
        return lessonService.getAll();
    }

    @GetMapping("/{id}")
    public ApiResponseDto<Lesson> getById(@PathVariable String id) {
        return lessonService.getById(id);
    }

    // Bước 1: Tạo Lesson (Vỏ)
    @PostMapping
    public ApiResponseDto<Lesson> create(@Valid @RequestBody LessonCreationDto req) {
        return lessonService.create(req);
    }

    // Bước 2: Thêm Section và Ảnh vào LessonID tương ứng
    @PostMapping(
            value = "/{lessonId}/sections",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponseDto<?> addSection(
            @PathVariable String lessonId,
            @RequestParam(required = false) String parentId,
            @RequestPart("section") LessonSectionCreateDto sectionDto,
            @RequestPart(value = "images", required = false) MultipartFile[] images
    ) throws IOException {

        LessonSection section = lessonService.addSection(
                lessonId, parentId, sectionDto, images
        );

        return ApiResponseDto.builder()
                .status(200)
                .message("Thêm mục bài học thành công")
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponseDto<Void> deleteLesson(@PathVariable String id) {
        lessonRepository.deleteById(id);
        return ApiResponseDto.<Void>builder()
                .status(200)
                .message("Đã xóa bài học và toàn bộ nội dung liên quan")
                .build();
    }

    // Xóa một mục cụ thể (Sẽ xóa mục đó và các mục con của nó)
    @DeleteMapping("/sections/{sectionId}")
    public ApiResponseDto<Void> deleteSection(@PathVariable String sectionId) {
        lessonService.deleteSection(sectionId);
        return ApiResponseDto.<Void>builder()
                .status(200)
                .message("Đã xóa mục nội dung thành công")
                .build();
    }

    @PutMapping("/sections/{id}")
    public ResponseEntity<?> updateSection(
            @PathVariable String id,
            @RequestPart("section") LessonSection data, // Nhận JSON từ Blob
            @RequestPart(value = "images", required = false) MultipartFile[] images // Nhận File ảnh
    ) {
        try {
            // Gọi Service xử lý logic cập nhật
            LessonSection updatedSection = lessonService.updateSection(id, data, images);

            return ResponseEntity.ok(ApiResponseDto.builder()
                    .status(200)
                    .message("Cập nhật mục nội dung thành công!")
                    .data(updatedSection)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponseDto.builder()
                    .status(500)
                    .message("Lỗi khi cập nhật: " + e.getMessage())
                    .build());
        }
    }
}