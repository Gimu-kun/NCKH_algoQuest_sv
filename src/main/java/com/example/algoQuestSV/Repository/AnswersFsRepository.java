package com.example.algoQuestSV.Repository;

import com.example.algoQuestSV.Entity.AnswersFs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnswersFsRepository extends JpaRepository<AnswersFs,Integer> {
}
