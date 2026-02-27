package com.example.algoQuestSV.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "quests_rewards")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestReward {
    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quest_id")
    @JsonIgnoreProperties({"lessons", "questions", "prerequisites", "handler", "hibernateLazyInitializer"})
    private Quest quest;

    @Column(name = "reward_exp")
    private Integer rewardExp;

    @Column(name = "reward_point")
    private Integer rewardPoint;

    @Column(name = "reward_gold")
    private Integer rewardGold;

    @Column(name = "reward_stone")
    private Integer rewardStone;

    @Column(name = "reward_wood")
    private Integer rewardWood;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = "RW-" + UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        }
        if (rewardExp == null) rewardExp = 0;
        if (rewardPoint == null) rewardPoint = 0;
        if (rewardGold == null) rewardGold = 0;
        if (rewardStone == null) rewardStone = 0;
        if (rewardWood == null) rewardWood = 0;
    }
}