package com.example.algoQuestSV.Dto.Quest;

import com.example.algoQuestSV.Entity.Quest;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuestUnlockStatusDto {
    private Quest quest;
    private boolean isUnlocked;
    private boolean isCompleted;
}
