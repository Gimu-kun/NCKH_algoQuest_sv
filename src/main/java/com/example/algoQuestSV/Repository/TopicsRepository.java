package com.example.algoQuestSV.Repository;

import com.example.algoQuestSV.Entity.Topic;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TopicsRepository extends JpaRepository<Topic,String> {
    @Query("SELECT MAX(t.indexOrder) FROM Topic t")
    Optional<Integer> findMaxIndexOrder();

    List<Topic> findAll(Sort sort);

    boolean existsByTitle(String title);

    boolean existsByTitleAndIdNot(String title, String id);

    Optional<Topic> findById(String id);
}
