package com.example.algoQuestSV.Dto.Multiplay;

import lombok.Data;

@Data
public class TeamBattleResult {
    private Long teamId;
    private String player1Id;
    private String player2Id;
    private double executionTime; // ms
    private int testcasePassed;
    private int completionTimeSeconds; // Thời gian hoàn thành từ lúc bắt đầu
}
