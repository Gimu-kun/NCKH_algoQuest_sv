package com.example.algoQuestSV.Dto.Visualization;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class VisualizationSubmitDto {
    private String visualizationId;
    @JsonProperty("isCorrect")
    private boolean isCorrect;
    private int correctOp;
    private int incorrectOp;
    private String selectedAnswer;
    private int difficulty; // 1: Easy, 2: Medium
}
