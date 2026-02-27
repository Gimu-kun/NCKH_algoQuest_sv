package com.example.algoQuestSV.Dto.Visualization;

import lombok.Data;

@Data
public class QuestVisualLinkRequestDto {
    private String questId;
    private String visualizationId;
    private Integer point;
    private Integer exp;
    private Integer gold;
    private Integer wood;
    private Integer stone;
}