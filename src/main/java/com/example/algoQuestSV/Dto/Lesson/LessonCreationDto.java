package com.example.algoQuestSV.Dto.Lesson;

import com.example.algoQuestSV.Dto.Quest.QuestCreationDto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LessonCreationDto {
    @Length(min = 4)
    private String title;

    @NotNull(message = "Nội dung bài học không được để trống")
    private String content;

    private List<MultipartFile> images;

    private String operatorId;
}
