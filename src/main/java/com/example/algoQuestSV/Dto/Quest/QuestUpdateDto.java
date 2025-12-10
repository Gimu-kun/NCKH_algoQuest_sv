package com.example.algoQuestSV.Dto.Quest;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuestUpdateDto {
    private String topicId;

    private String title;

    private Boolean status;

    private String description;

    @NotNull(message = "ID người thao tác không được trống!")
    private String operatorId;
}
