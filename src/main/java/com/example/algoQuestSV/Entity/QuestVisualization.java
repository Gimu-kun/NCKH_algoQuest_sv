package com.example.algoQuestSV.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "quests_visualizations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestVisualization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quest_id", referencedColumnName = "id")
    private Quest quest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visualization_id", referencedColumnName = "id")
    private Visualization visualization;

    @Column(name = "point")
    private Integer point = 0;

    @Column(name = "exp")
    private Integer exp = 0;

    @Column(name = "stone")
    private Integer stone = 0;

    @Column(name = "gold")
    private Integer gold = 0;

    @Column(name = "wood")
    private Integer wood = 0;
}