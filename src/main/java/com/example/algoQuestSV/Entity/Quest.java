package com.example.algoQuestSV.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Builder
@Table(name = "quests")
public class Quest {
    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "topic_id")
    private Topic topicId;

    @Length(min = 10, message = "Tiêu đề ải tối thiểu phải có 10 chữ cái")
    @NotNull(message = "Tiêu đề ải không được để trống!")
    private String title;

    private Boolean status = false;

    private String description;

    @Column(name = "index_order")
    private Integer indexOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", referencedColumnName = "id")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", referencedColumnName = "id")
    private User updatedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) id = "Q-" + UUID.randomUUID().toString().replace("-", "").substring(0,5);
        if (createdAt == null) createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
