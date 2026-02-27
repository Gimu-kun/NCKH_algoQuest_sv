package com.example.algoQuestSV.Dto.Quest;
import lombok.Data;

@Data
public class QuestRewardRequestDto {
    private String questId;
    private Integer rewardExp;
    private Integer rewardGold;
    private Integer rewardStone;
    private Integer rewardWood;
}
