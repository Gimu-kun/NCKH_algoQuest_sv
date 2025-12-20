package com.example.algoQuestSV.Repository;

import com.example.algoQuestSV.Entity.Visualization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VisualizationsRepository extends JpaRepository<Visualization,String> {
}
