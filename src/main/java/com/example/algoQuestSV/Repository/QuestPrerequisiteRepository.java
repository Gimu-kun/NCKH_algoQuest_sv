package com.example.algoQuestSV.Repository;

import com.example.algoQuestSV.Entity.QuestPrerequisite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestPrerequisiteRepository extends JpaRepository<QuestPrerequisite,String> {
    List<QuestPrerequisite> findByQuestId(String questId);
    void deleteByQuestId(String questId);
}
