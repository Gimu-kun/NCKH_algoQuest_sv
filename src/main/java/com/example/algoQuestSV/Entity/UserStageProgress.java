package com.example.algoQuestSV.Entity;

import com.example.algoQuestSV.Enum.ChallengeStatus;
import com.example.algoQuestSV.Enum.StageType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_stage_progress")
@Data
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
}
