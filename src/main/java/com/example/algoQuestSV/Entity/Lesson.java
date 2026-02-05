package com.example.algoQuestSV.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "lessons")
@AllArgsConstructor
@NoArgsConstructor
public class Lesson {
    @Id
    private String id;

    @Length(min = 4)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "quests", "lessons"})
    private Topic topic;

    // Quan hệ với các Section cấp 1 (H1), tự động xóa con khi xóa cha
    @OneToMany(mappedBy = "lessonId", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<LessonSection> sections;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    @JsonIgnoreProperties({
            "passwords", "streaks", "hibernateLazyInitializer",
            "handler", "firstName", "lastName", "avatar",
            "role", "level", "exp", "woods", "stones", "point", "gold"
    })
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    @JsonIgnoreProperties({
            "passwords", "streaks", "hibernateLazyInitializer",
            "handler", "firstName", "lastName", "avatar",
            "role", "level", "exp", "woods", "stones", "point", "gold"
    })
    private User updatedBy;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) id = "L-" + UUID.randomUUID().toString().substring(0, 8);
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}