package com.example.algoQuestSV.Repository;

import com.example.algoQuestSV.Entity.AnswersFn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnswersFnRepository extends JpaRepository<AnswersFn,Integer> {
}
