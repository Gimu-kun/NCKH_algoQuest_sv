package com.example.algoQuestSV.Dto.Visualization;

import lombok.Data;

@Data
public class VisualizationRequestDto {
    private String id;
    private String visualizationType;
    private String data;
    private String templateCode;
}