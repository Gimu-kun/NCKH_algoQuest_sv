package com.example.algoQuestSV.Dto.Lesson;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LessonImgsDto {
    @NotNull(message = "ID bài học không được trống")
    private String lessonId;

    @NotNull(message = "Đường dẫn hình ảnh không được trống")
    private String url;

    private Integer indexOrder;
}
