package com.example.algoQuestSV.Entity;

import com.example.algoQuestSV.Enum.ChallengeStatus;
import com.example.algoQuestSV.Enum.StageType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "user_stage_progress")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStageProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id")
    private Long sessionId;

    @Column(name = "stage_num")
    private Integer stageNum;

    @Enumerated(EnumType.STRING)
    private ChallengeStatus status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "stage_type")
    @Enumerated(EnumType.STRING)
    private StageType stageType;

    @Column(name = "remain_life")
    private Integer remainLife;

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @JoinColumn(name = "progress_id", insertable = false, updatable = false)
    private List<UserStageQuestions> questions;

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @JoinColumn(name = "progress_id", insertable = false, updatable = false)
    private List<UserStageVisualization> visualization;

    private boolean pass;

    private Integer gold = 0;

    private Integer stone = 0;

    private Integer point = 0;

    private Integer wood = 0;

    private Integer exp = 0;

}
