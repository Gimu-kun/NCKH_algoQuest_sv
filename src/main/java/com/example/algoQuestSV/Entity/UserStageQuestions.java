package com.example.algoQuestSV.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_stage_questions")
@Data
public class UserStageQuestions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "progress_id")
    private Integer progressId;

    @Column(name = "question_id")
    private Integer questionId;

    @Column(name = "user_answer")
    private String userAnswer;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Column(name = "is_answered")
    private Boolean isAnswered;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;
}
