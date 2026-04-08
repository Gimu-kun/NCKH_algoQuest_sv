package com.example.algoQuestSV.Repository;

import com.example.algoQuestSV.Entity.ChallengeSession;
import com.example.algoQuestSV.Enum.ChallengeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChallengeSessionRepository extends JpaRepository<ChallengeSession, Long> {
    List<ChallengeSession> findAllByUserIdAndStatusOrderByIdDesc(String userId, ChallengeStatus challengeStatus);
}
