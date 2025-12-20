package com.example.algoQuestSV.Dto.Answer;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnswerFnCreationDto {
    private Double answer;
    private Double tolerance;
}
