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
    AND v.id NOT IN (:excludeIds) AND v.difficulty = :difficulty
    ORDER BY RANDOM()
    LIMIT 1
    """, nativeQuery = true)
        Visualization findWithExclude(
                float minDiff,
                float maxDiff,
                int difficulty,
                List<String> excludeIds
    );

    @Query(value = """
    SELECT * FROM visualizations v
    WHERE (:minDiff <= COALESCE(1.0 - CAST(pass_count AS float) / NULLIF(pass_count + fail_count,0), 0.5))
    AND (COALESCE(1.0 - CAST(pass_count AS float) / NULLIF(pass_count + fail_count,0), 0.5) <= :maxDiff)
    AND v.difficulty = :difficulty
    ORDER BY RANDOM()
    LIMIT 1
    """, nativeQuery = true)
        Visualization findWithoutExclude(
                float minDiff,
                float maxDiff,
                int difficulty
    );
}

