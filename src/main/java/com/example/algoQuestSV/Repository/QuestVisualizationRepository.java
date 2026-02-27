package com.example.algoQuestSV.Repository;

import com.example.algoQuestSV.Entity.QuestVisualization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuestVisualizationRepository extends JpaRepository<QuestVisualization,Integer> {
    Optional<QuestVisualization> findByQuestIdAndVisualizationId(String questId, String visualizationId);
    List<QuestVisualization> findByQuestId(String questId);
}
