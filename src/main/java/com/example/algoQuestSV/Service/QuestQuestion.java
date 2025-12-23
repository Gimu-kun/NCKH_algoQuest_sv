package com.example.algoQuestSV.Service;

import com.example.algoQuestSV.Entity.Quest;
import com.example.algoQuestSV.Entity.Question;
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

    private Double point;
    private Double exp;
}