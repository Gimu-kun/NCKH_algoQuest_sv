package com.example.algoQuestSV.Repository;

import com.example.algoQuestSV.Entity.Lesson;
import com.example.algoQuestSV.Entity.LessonSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson,String> {
    @Query("SELECT s FROM LessonSection s WHERE s.lessonId = :lessonId AND s.parent IS NULL")
    List<LessonSection> findRootSections(@Param("lessonId") String lessonId);
}
