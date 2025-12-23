package com.example.algoQuestSV.Repository;

import com.example.algoQuestSV.Entity.Quest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface QuestsRepository extends JpaRepository<Quest,String> {
    boolean existsByTitleAndIdNot(String title, String id);

    Optional<Quest> findTopByTopicId_IdOrderByIndexOrderDesc(String id);

    @Modifying
    @Query("UPDATE Quest q SET q.indexOrder = :indexOrder WHERE q.id = :id")
    void updateIndexOrder(@Param("id") String id, @Param("indexOrder") Integer indexOrder);
}
