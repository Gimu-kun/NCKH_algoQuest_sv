package com.example.algoQuestSV.Dto.Quiz;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuizResultDetail {
    private String questionId;
    private boolean isCorrect;
    private Object userChoice;
    private Object correctAnswer;
    private Integer earnedExp;
    private Integer earnedPoint;
    private Integer earnedGold;
    private Integer earnedStone;
    private Integer earnedWood;
}