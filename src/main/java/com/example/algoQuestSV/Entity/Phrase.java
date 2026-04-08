package com.example.algoQuestSV.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "phrases")
@Data
public class Phrase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phrase_num")
    private Integer phraseNum;

    @Column(name = "start_stage")
    private Integer startStage;

    @Column(name = "end_stage")
    private Integer endStage;

    @Column(name = "min_diff")
    private Integer minDiff;

    @Column(name = "max_diff")
    private Integer maxDiff;

    @Column(name = "bloom_distribution")
    private String bloomDistribution;
}
