package com.example.algoQuestSV.Dto.Challenge;

import com.example.algoQuestSV.Enum.StageDifficulty;
import com.example.algoQuestSV.Enum.StageType;
import lombok.Data;

@Data
public class StageGeneralType {
    private Integer stageNum;
    private Integer phraseNum;
    private StageType stageType;
    private StageDifficulty difficulty;
}
