package com.example.algoQuestSV.Repository;

import com.example.algoQuestSV.Entity.QuestAnswerStorage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuestAnswerStorageRepository extends JpaRepository<QuestAnswerStorage,Long> {
    Optional<QuestAnswerStorage> findByQuestProgressId(String progressId);
}
