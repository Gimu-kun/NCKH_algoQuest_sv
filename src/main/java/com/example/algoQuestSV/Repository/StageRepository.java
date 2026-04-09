package com.example.algoQuestSV.Repository;

import com.example.algoQuestSV.Entity.Stage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StageRepository extends JpaRepository<Stage, Long> {
    Optional<Stage> findByStageNum(int stageNum);

    List<Stage> findAllByOrderByStageNumAsc();
}
