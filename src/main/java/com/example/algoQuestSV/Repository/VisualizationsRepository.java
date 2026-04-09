package com.example.algoQuestSV.Repository;

import com.example.algoQuestSV.Entity.Visualization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VisualizationsRepository extends JpaRepository<Visualization,String> {
    @Query(value = """
    SELECT * FROM visualizations v
    WHERE (:minDiff <= COALESCE(1.0 - CAST(pass_count AS float) / NULLIF(pass_count + fail_count,0), 0.5))
    AND (COALESCE(1.0 - CAST(pass_count AS float) / NULLIF(pass_count + fail_count,0), 0.5) <= :maxDiff)
    AND (COALESCE(:excludeIds, NULL) IS NULL OR v.id NOT IN (:excludeIds))
    ORDER BY RANDOM()
    LIMIT 1
    """, nativeQuery = true)
    Visualization findRandomByDifficultyRange(
            @Param("minDiff") float minDiff,
            @Param("maxDiff") float maxDiff,
            @Param("excludeIds") List<String> excludeIds
    );
}
