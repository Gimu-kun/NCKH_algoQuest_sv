package com.example.algoQuestSV.Repository;

import com.example.algoQuestSV.Entity.Quest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuestsRepository extends JpaRepository<Quest,String> {
    boolean existsByTitleAndIdNot(String title, String id);

    Optional<Quest> findTopByTopicId_IdOrderByIndexOrderDesc(String id);
}
