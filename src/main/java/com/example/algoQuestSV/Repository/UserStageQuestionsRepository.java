package com.example.algoQuestSV.Repository;

import com.example.algoQuestSV.Entity.UserStageQuestions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserStageQuestionsRepository extends JpaRepository<UserStageQuestions, Long> {
    List<UserStageQuestions> findAllByProgressId(Long id);
}
