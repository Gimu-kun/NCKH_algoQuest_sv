package com.example.algoQuestSV.Dto.Challenge;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResultDetailDTO {
    private String questionId;
    private boolean isCorrect;
    private String questionContent;
    private Object userAnswer;
    private Object correctAnswer;
}
