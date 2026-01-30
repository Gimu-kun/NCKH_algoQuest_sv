package com.example.algoQuestSV.Dto.Answer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerFnCreationDto {
    private String id;
    private String questionId;
    private Double answer;
    private Double tolerance;
}
