package com.example.algoQuestSV.Entity;

import com.example.algoQuestSV.Enum.StageDifficulty;
import com.example.algoQuestSV.Enum.StageType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "stages")
@Data
public class Stage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Integer stageNum;

    @Column(name = "phrase_id")
    private Long phraseId;

    @Enumerated(EnumType.STRING)
    private StageType stageType;

    @Enumerated(EnumType.STRING)
    private StageDifficulty difficulty;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
