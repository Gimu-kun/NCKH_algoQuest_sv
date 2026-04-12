package com.example.algoQuestSV.Dto.Challenge;

import com.example.algoQuestSV.Entity.Question;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResultDTO {
    private Question question;
    private boolean isCorrect;
    private String userAnswer;
}
