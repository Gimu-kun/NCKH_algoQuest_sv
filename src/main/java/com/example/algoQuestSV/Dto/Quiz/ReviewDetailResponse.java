package com.example.algoQuestSV.Dto.Quiz;

import com.example.algoQuestSV.Entity.QuestProgress;
import com.example.algoQuestSV.Entity.Question;
import com.example.algoQuestSV.Enum.BloomType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ReviewDetailResponse {
    private QuestProgress progress;
    private List<QuestionReviewDto> details;

    @Data
    @Builder
    public static class QuestionReviewDto {
        private Question question;
        private Object userChoice;
        private Object correctAnswer;
        private boolean isCorrect;
        private List<Object> options;
    }
}
