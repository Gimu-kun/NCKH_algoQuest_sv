package com.example.algoQuestSV.Repository;

import com.example.algoQuestSV.Entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonRepository extends JpaRepository<Lesson,String> {
}
