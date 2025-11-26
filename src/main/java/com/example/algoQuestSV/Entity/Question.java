package com.example.algoQuestSV.Entity;

import com.example.algoQuestSV.Enum.QuestionType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Builder
@Table(name = "questions")
public class Question {
    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "quest_id")
    private Quest questId;

    private QuestionType questionType;

    private Boolean status = false;

    @Column(name = "question_content")
    private String questionContent;

    private Integer score = 0;

    @Column(name = "index_order")
    private Integer indexOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", referencedColumnName = "id")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", referencedColumnName = "id")
    private User updatedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "questionId", cascade = CascadeType.ALL)
    private List<QuestionImgs> questionImgs;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
