package com.example.algoQuestSV.Dto.Topic;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class TopicCreationDto {
    @Length(min = 10, message = "Tiêu đề chương tối thiểu phải có 10 chữ cái")
    @NotNull(message = "Tiêu đề chương không được để trống!")
    private String title;

    private String description;

    private String operatorId;
}
