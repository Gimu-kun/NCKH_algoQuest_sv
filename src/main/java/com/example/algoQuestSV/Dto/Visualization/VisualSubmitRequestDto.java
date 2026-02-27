package com.example.algoQuestSV.Dto.Visualization;

import lombok.Data;

@Data
public class VisualSubmitRequestDto {
    private String userId;
    private String questId;
    private String visualizationId;
    private String selectedAnswer;
}