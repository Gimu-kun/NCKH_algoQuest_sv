package com.example.algoQuestSV.Dto.Lesson;

import lombok.Data;

import java.util.List;

@Data
public class LessonSectionResponseDto {
    private String id;
    private String title;
    private String content;
    private Integer level;
    private Integer orderIndex;
    private List<LessonSectionResponseDto> children;
}