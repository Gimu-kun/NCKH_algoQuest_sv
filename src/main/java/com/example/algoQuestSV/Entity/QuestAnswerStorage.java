package com.example.algoQuestSV.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "quest_answer_storage")
public class QuestAnswerStorage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "quest_progress_id")
    private QuestProgress questProgress;

    @Column(columnDefinition = "json")
    private String answerData; // Chứa quizAnswers gửi từ FE

    @Column(columnDefinition = "json")
    private String resultDetail; // Chứa mảng đúng/sai/đáp án đúng

}
