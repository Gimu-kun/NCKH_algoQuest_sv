package com.example.algoQuestSV.Repository;

import com.example.algoQuestSV.Entity.Quest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestsRepository extends JpaRepository<Quest,String> {
}
