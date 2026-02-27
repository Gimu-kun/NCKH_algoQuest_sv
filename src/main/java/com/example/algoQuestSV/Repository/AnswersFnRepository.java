package com.example.algoQuestSV.Repository;

import com.example.algoQuestSV.Entity.AnswersFn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface AnswersFnRepository extends JpaRepository<AnswersFn,Integer> {
    List<AnswersFn> findByQuestionId(String qId);
}
