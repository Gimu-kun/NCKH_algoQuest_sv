package com.example.algoQuestSV.Repository;

import com.example.algoQuestSV.Entity.AnswersFns;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnswersFnsRepository extends JpaRepository<AnswersFns,Integer> {
}
