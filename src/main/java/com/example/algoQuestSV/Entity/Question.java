package com.example.algoQuestSV.Entity;

import com.example.algoQuestSV.Enum.BloomType;
import com.example.algoQuestSV.Enum.QuestionType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "questions")
public class Question {
    @Id
    private String id;

    private String topicId;

    @Enumerated(EnumType.STRING)
    private QuestionType questionType;

    @Enumerated(EnumType.STRING)
    private BloomType bloom;

    private Boolean status = false;

    @Column(name = "question_content")
    private String questionContent;

    @Column(name = "index_order")
    private Integer indexOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({
            "passwords", "streaks", "hibernateLazyInitializer",
            "handler", "firstName", "lastName", "avatar",
            "role", "level", "exp", "woods", "stones", "point", "gold"
    })
    @JoinColumn(name = "created_by", referencedColumnName = "id")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({
            "passwords", "streaks", "hibernateLazyInitializer",
            "handler", "firstName", "lastName", "avatar",
            "role", "level", "exp", "woods", "stones", "point", "gold"
    })
    @JoinColumn(name = "updated_by", referencedColumnName = "id")
    private User updatedBy;

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @JoinColumn(name = "question_id", insertable = false, updatable = false)
    private List<AnswersFn> fnAnswers;

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @JoinColumn(name = "question_id", insertable = false, updatable = false)
    private List<AnswersFns> fnsAnswers;

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @JoinColumn(name = "question_id", insertable = false, updatable = false)
    private List<AnswersFs> fsAnswers;

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @JoinColumn(name = "question_id", insertable = false, updatable = false)
    private List<AnswersMcq> mcqAnswers;

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @JoinColumn(name = "question_id", insertable = false, updatable = false)
    private List<AnswersMp> mpAnswers;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "questionId", cascade = CascadeType.ALL)
    private List<QuestionImgs> questionImgs;

    @PrePersist
    protected void onCreate() {
        status = false;
        if (id == null) id = "QS-" + UUID.randomUUID().toString().replace("-", "").substring(0,5);
        if (createdAt == null) createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
