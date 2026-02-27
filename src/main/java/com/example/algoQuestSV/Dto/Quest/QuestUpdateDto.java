package com.example.algoQuestSV.Dto.Quest;

import com.example.algoQuestSV.Enum.QuestType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuestUpdateDto {
    private String topicId;

    private QuestType type;

    private Integer questNum;

    private String title;

    private Boolean status;

    private String description;

    @NotNull(message = "ID người thao tác không được trống!")
    private String operatorId;
}
