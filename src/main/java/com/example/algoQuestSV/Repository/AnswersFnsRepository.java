package com.example.algoQuestSV.Repository;

import com.example.algoQuestSV.Entity.AnswersFns;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface AnswersFnsRepository extends JpaRepository<AnswersFns,Integer> {
    List<AnswersFns> findByQuestionId(String qId);
}
