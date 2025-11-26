package com.example.algoQuestSV.Repository;

import com.example.algoQuestSV.Entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionsRepository extends JpaRepository<Question,String> {
}
