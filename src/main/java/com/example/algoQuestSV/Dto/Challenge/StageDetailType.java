package com.example.algoQuestSV.Dto.Challenge;

import com.example.algoQuestSV.Entity.Question;
import com.example.algoQuestSV.Entity.Visualization;
import com.example.algoQuestSV.Enum.StageType;
import lombok.Data;

import java.util.List;

@Data
public class StageDetailType {
    private Long progressId;
    private StageType type;
    private List<Question> questions;
    private Visualization visualization;
    private Integer remainLife;
}
