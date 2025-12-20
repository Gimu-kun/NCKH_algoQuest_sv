package com.example.algoQuestSV.Dto.Answer;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnswerMcqCreationDto {
    private String content;
    private Boolean isCorrect;
}
