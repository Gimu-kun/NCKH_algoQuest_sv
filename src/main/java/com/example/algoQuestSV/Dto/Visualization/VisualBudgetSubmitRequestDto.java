package com.example.algoQuestSV.Dto.Visualization;

import lombok.Data;

@Data
public class VisualBudgetSubmitRequestDto {
    private String userId;
    private String questId;
    private String visualizationId;
    private String selectedOptionId;
    private Integer actualSteps;
}