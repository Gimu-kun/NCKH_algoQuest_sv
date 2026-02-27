package com.example.algoQuestSV.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "quest_progress")
public class QuestProgress {

    @Id
    @Column(columnDefinition = "bpchar(8)")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quest_id")
    @JsonIgnoreProperties({"lessons", "questions", "prerequisites", "handler", "hibernateLazyInitializer"})
    private Quest quest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({
            "passwords", "streaks", "hibernateLazyInitializer",
            "handler", "firstName", "lastName"
    })
    private User user;

    @Column(name = "is_completed")
    private Boolean isCompleted;

    @Column(name = "earned_exp")
    private Integer earnedExp;

    @Column(name = "earned_point")
    private Integer earnedPoint;

    @Column(name = "earned_gold")
    private Integer earnedGold;

    @Column(name = "earned_stone")
    private Integer earnedStone;

    @Column(name = "earned_wood")
    private Integer earnedWood;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        // Thiết lập giá trị mặc định giống như trong SQL Definition
        if (isCompleted == null) isCompleted = false;
        if (earnedExp == null) earnedExp = 0;
        if (earnedPoint == null) earnedPoint = 0;
        if (earnedGold == null) earnedGold = 0;
        if (earnedStone == null) earnedStone = 0;
        if (earnedWood == null) earnedWood = 0;

        // Tạo ID 8 ký tự nếu chưa có
        if (id == null) {
            id = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        }

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
