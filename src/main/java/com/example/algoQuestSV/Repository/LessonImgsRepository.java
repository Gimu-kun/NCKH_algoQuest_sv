package com.example.algoQuestSV.Repository;

import com.example.algoQuestSV.Entity.LessonImgs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonImgsRepository extends JpaRepository<LessonImgs,String> {
}
