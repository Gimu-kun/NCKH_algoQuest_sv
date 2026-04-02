package com.example.algoQuestSV.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class levelConfig {
    @Id
    private Integer level;

    @Column(name = "require_exp")
    private Integer requiredExp;

    @Column(name = "reward_gold")
    private Integer rewardGold;

    @Column(name = "reward_stone")
    private Integer rewardStone;

    @Column(name = "reward_point")
    private Integer rewardPoint;

    @Column(name = "reward_wood")
    private Integer rewardWood;
}
