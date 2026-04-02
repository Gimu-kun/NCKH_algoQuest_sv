package com.example.algoQuestSV.Repository;

import com.example.algoQuestSV.Dto.Quiz.MaxRewardProjection;
import com.example.algoQuestSV.Entity.QuestProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestProgressRepository extends JpaRepository<QuestProgress, String> {
    List<QuestProgress> findByUserId(String userId);
    Optional<QuestProgress> findByUserIdAndQuestId(String userId, String questId);

    List<QuestProgress> findAllByUserIdAndQuestId(String id, String id1);

    boolean existsByUserIdAndQuestIdAndIsCompletedTrue(String id, String id1);

    @Query("SELECT MAX(p.earnedExp) as maxEarnedExp, " +
            "MAX(p.earnedPoint) as maxEarnedPoint, " +
            "MAX(p.earnedGold) as maxEarnedGold, " +
            "MAX(p.earnedStone) as maxEarnedStone, " +
            "MAX(p.earnedWood) as maxEarnedWood " +
            "FROM QuestProgress p " +
            "WHERE p.user.id = :userId AND p.quest.id = :questId AND p.isCompleted = true")
    MaxRewardProjection findMaxRewardsByUserIdAndQuestId(String userId, String questId);
    List<QuestProgress> findByUserIdAndQuestIdOrderByCreatedAtDesc(String userId, String questId);

    boolean existsByUserIdAndQuestId(String userId, String stageId);

    QuestProgress findTopByUserIdAndQuestIdOrderByCreatedAtDesc(String userId, String stageId);
}
