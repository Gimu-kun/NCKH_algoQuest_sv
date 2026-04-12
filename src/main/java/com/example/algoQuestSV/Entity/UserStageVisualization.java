package com.example.algoQuestSV.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_stage_visualization")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    @Column(name = "selected_answer")
    private String selectedAnswer;
}
