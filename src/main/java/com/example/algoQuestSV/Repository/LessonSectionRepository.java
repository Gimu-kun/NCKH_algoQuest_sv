package com.example.algoQuestSV.Repository;

import com.example.algoQuestSV.Entity.LessonSection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonSectionRepository extends JpaRepository<LessonSection,String> {
    int countByLessonIdAndParentIdIsNull(String lessonId);

    int countByParentId(String parentId);

    int countByLessonIdAndParentIsNull(String lessonId);
}
