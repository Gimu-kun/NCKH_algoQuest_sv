package com.example.algoQuestSV.Repository;

import com.example.algoQuestSV.Entity.AnswersMcq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnswersMcqRepository extends JpaRepository<AnswersMcq,Integer> {
}
