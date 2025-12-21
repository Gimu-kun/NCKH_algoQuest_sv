package com.example.algoQuestSV.Dto.Answer;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerMcqCreationDto {
    private String content;
    private Boolean isCorrect;
}
