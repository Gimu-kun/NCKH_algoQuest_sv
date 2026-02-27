package com.example.algoQuestSV.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "quest_prerequisites")
public class QuestPrerequisite {
    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quest_id", nullable = false)
    @JsonIgnoreProperties({"lessons", "questions", "topicId"})
    private Quest quest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "required_quest_id")
    private Quest requiredQuest;

    @Column(name = "required_level")
    private Integer requiredLevel;

    @PrePersist
    protected void onCreate() {
        if (id == null) id = "QP-" + UUID.randomUUID().toString().replace("-", "").substring(0, 6);
    }
}
