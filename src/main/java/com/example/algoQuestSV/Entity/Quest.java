package com.example.algoQuestSV.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "quests")
public class Quest {
    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "topic_id")
    @JsonIgnoreProperties({"quests", "handler", "hibernateLazyInitializer"})
    private Topic topicId;

    @Length(min = 10, message = "Tiêu đề ải tối thiểu phải có 10 chữ cái")
    @NotNull(message = "Tiêu đề ải không được để trống!")
    private String title;

    private Boolean status;

    private String description;

    @Column(name = "index_order")
    private Integer indexOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", referencedColumnName = "id")
    @JsonIgnoreProperties({
            "passwords", "streaks", "hibernateLazyInitializer",
            "handler", "firstName", "lastName", "avatar",
            "role", "level", "exp", "woods", "stones", "point", "gold"
    })
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", referencedColumnName = "id")
    @JsonIgnoreProperties({
            "passwords", "streaks", "hibernateLazyInitializer",
            "handler", "firstName", "lastName", "avatar",
            "role", "level", "exp", "woods", "stones", "point", "gold"
    })
    private User updatedBy;

    @OneToMany(mappedBy = "quest", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("quest")
    private List<QuestLesson> lessons = new ArrayList<>();

    @OneToMany(mappedBy = "quest", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("quest")
    private List<QuestQuestion> questions = new ArrayList<>();

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        status = false;
        if (id == null) id = "Q-" + UUID.randomUUID().toString().replace("-", "").substring(0,6);
        if (createdAt == null) createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
