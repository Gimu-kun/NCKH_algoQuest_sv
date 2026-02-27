package com.example.algoQuestSV.Repository;

import com.example.algoQuestSV.Entity.QuestReward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestRewardRepository extends JpaRepository<QuestReward, String> {
    List<QuestReward> findByQuestId(String questId);
}