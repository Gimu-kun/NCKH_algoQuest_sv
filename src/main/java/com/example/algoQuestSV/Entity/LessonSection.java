package com.example.algoQuestSV.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "lesson_sections")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonSection {
    @Id
    private String id;

    @Column(name = "lesson_id")
    private String lessonId;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Min(1)
    private Integer level; // Cấp độ: 1 (H1), 2 (H2),...

    private Integer orderIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnore // Tránh vòng lặp vô tận khi trả về JSON
    private LessonSection parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<LessonSection> children;

    @OneToMany(mappedBy = "sectionId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LessonImgs> images;

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Refs> refs = new ArrayList<>();

    @PrePersist
    public void onCreate() {
        if (id == null) id = "SEC-" + UUID.randomUUID().toString().substring(0, 8);
    }
}