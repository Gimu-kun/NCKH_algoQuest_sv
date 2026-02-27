package com.example.algoQuestSV.Dto.Quiz;

import lombok.Data;

import java.util.Map;

@Data
public class UserAnswerDto {
    private String questionId;
    private String questionType;
    private String selectedMcqId;
    private String textAnswer;
    private Map<String, String> mpMatches;
}
