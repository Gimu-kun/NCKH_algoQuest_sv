package com.example.algoQuestSV.Repository;

import com.example.algoQuestSV.Entity.Streak;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StreaksRepository extends JpaRepository<Streak, Integer> {
    Optional<Streak> findFirstByUserIdOrderByAccessAtDesc(String userId);
}
