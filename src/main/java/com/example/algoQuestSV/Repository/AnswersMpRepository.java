package com.example.algoQuestSV.Repository;

import com.example.algoQuestSV.Entity.AnswersMp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnswersMpRepository extends JpaRepository<AnswersMp,Integer> {
}
