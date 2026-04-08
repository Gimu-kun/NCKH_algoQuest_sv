package com.example.algoQuestSV.Repository;

import com.example.algoQuestSV.Entity.UserStageProgress;
import com.example.algoQuestSV.Enum.ChallengeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserStageProgressRepository extends JpaRepository<UserStageProgress, Long> {
    UserStageProgress findAllBySessionIdAndStatusOrderByIdDesc(Long id, ChallengeStatus challengeStatus);
}
