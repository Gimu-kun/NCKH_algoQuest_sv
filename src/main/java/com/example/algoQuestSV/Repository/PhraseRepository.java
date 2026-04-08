package com.example.algoQuestSV.Repository;

import com.example.algoQuestSV.Entity.Phrase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhraseRepository extends JpaRepository<Phrase, Long> {
}
