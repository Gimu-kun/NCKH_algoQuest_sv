package com.example.algoQuestSV.Dto.Answer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerMpCreationDto {
    private String column1;
    private String column2;
}
