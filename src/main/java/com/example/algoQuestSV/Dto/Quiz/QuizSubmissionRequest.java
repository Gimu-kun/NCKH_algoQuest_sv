package com.example.algoQuestSV.Dto.Quiz;

import lombok.Data;

import java.util.List;

@Data
public class QuizSubmissionRequest {
    private String questId;
    private String userId;
    private List<UserAnswerDto> userAnswer;
}
