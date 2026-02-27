package com.example.algoQuestSV.Dto.Quiz;

import com.example.algoQuestSV.Entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuizResultResponse {
    private Integer correctCount;
    private Integer totalCount;
    private Integer preMaxExp;
    private Integer earnedExp;
    private Integer preMaxPoint;
    private Integer earnedPoint;
    private Integer preMaxGold;
    private Integer earnedGold;
    private Integer preMaxStone;
    private Integer earnedStone;
    private Integer preMaxWood;
    private Integer earnedWood;
    private Boolean isPassed;
    private User user;
    private List<QuizResultDetail> details;
}