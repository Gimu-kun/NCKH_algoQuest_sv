package com.example.algoQuestSV.Dto.Visualization;

import lombok.Data;

@Data
public class VisualCodeSubmitRequestDto {
    private String userId;
    private String questId;
    private String visualizationId;
    private String userCode;
}
