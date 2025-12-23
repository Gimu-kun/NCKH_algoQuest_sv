package com.example.algoQuestSV.Service;

import com.example.algoQuestSV.Entity.Lesson;
import com.example.algoQuestSV.Entity.Quest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "quests_lessons")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestLesson {
    @Id
    private String id = UUID.randomUUID().toString();

    @ManyToOne
    @JoinColumn(name = "quest_id")
    private Quest quest;

    @ManyToOne
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    private Double point;
    private Double exp;
}
