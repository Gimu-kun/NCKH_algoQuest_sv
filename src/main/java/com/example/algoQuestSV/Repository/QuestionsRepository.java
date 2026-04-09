package com.example.algoQuestSV.Repository;

import com.example.algoQuestSV.Entity.Question;
import com.example.algoQuestSV.Enum.BloomType;
import com.example.algoQuestSV.Enum.QuestionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionsRepository extends JpaRepository<Question,String> {
    @Query("""
    SELECT q FROM Question q
    WHERE q.bloom = :bloom
    AND q.questionType = :type
    AND q.topicId = :topic
    AND q.status = true
    AND (:excludeIds IS NULL OR q.id NOT IN :excludeIds)
""")
    List<Question> findAllWithFilter(
            @Param("bloom") BloomType bloomType,
            @Param("type") QuestionType questionType,
            @Param("topic") String topic,
            @Param("excludeIds") List<String> excludeIds
    );
}
