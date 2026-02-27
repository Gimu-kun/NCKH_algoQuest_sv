package com.example.algoQuestSV.Repository;

import com.example.algoQuestSV.Entity.AnswersFs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface AnswersFsRepository extends JpaRepository<AnswersFs,Integer> {
    List<AnswersFs> findByQuestionId(String qId);
}
