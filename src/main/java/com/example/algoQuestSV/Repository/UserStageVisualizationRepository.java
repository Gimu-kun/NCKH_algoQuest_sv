package com.example.algoQuestSV.Repository;

import com.example.algoQuestSV.Entity.UserStageVisualization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserStageVisualizationRepository extends JpaRepository<UserStageVisualization, Long> {
    UserStageVisualization findAllByProgressId(Long id);

    Optional<UserStageVisualization> findByProgressId(Long id);
}
