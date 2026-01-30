package com.example.algoQuestSV.Dto.Lesson;

import lombok.Data;

import java.util.List;

@Data
public class LessonSectionCreateDto {
    private String title;
    private String content;
    private Integer level;
    private List<RefRequestDto> refs;
}
