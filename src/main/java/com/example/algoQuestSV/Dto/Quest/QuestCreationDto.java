package com.example.algoQuestSV.Dto.Quest;

import com.example.algoQuestSV.Enum.QuestType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class QuestCreationDto {
    private QuestType questType;

    @Length(min = 10, message = "Tiêu đề ải tối thiểu phải có 10 chữ cái")
    @NotNull(message = "Tiêu đề ải không được để trống!")
    private String title;

    private String description;

    @NotNull(message = "ID người thao tác không được trống!")
    private String operatorId;

    @Override
    public String toString() {
        return "QuestCreationDto{" +
                ", questType=" + questType +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", operatorId='" + operatorId + '\'' +
                '}';
    }
}
