package com.example.algoQuestSV.Dto.Quest;

import lombok.Data;

@Data
public class QuestPrerequisiteRequestDto {
    private String questId;
    private String requiredQuestId;
    private Integer requiredLevel;
}