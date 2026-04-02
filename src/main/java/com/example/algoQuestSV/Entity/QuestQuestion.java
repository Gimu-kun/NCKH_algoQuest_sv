package com.example.algoQuestSV.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "quests_questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestQuestion {
    @Id
    private String id = UUID.randomUUID().toString();

    @ManyToOne
    @JoinColumn(name = "quest_id")
    private Quest quest;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;

    @Column(name = "default_mark")
    private Boolean defaultMark;

    private Integer point;
    private Integer exp;
    private Integer gold;
    private Integer stone;
    private Integer wood;
}