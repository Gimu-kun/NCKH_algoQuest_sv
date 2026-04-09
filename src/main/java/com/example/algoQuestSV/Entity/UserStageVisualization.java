package com.example.algoQuestSV.Entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

@Entity
@Table(name = "user_stage_visualization")
@Data
@Builder
public class UserStageVisualization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "progress_id")
    private Long progressId;

    @Column(name = "visualization_id")
    private String visualizationId;

    @Column(name = "is_passed")
    private Boolean isPassed;

    @Column(name = "correct_operation")
    private Integer correctOp;

    @Column(name = "incorrect_operation")
    private Integer incorrectOp;
}
