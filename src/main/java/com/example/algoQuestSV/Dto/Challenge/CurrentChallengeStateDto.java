package com.example.algoQuestSV.Dto.Challenge;

import lombok.Data;

@Data
public class CurrentChallengeStateDto {
    private String userId;
    private Long progressId;
    private Integer currentStage;
    private Integer remainLife;
    private Long sessionId;
}
