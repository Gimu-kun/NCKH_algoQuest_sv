package com.example.algoQuestSV.Dto.Challenge;

import lombok.Data;

import java.util.Map;

@Data
public class SubmitAnswerDTO {
    private String questionId;
    private String questionType;

    private String selectedMcqId;
    private String textAnswer;
    private Map<String, String> mpMatches;
}