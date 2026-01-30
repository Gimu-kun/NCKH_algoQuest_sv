package com.example.algoQuestSV.Entity;

import com.example.algoQuestSV.Enum.RefType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Refs {
    @Id
    @Column(length = 20)
    private String id;

    @Enumerated(EnumType.STRING)
    private RefType type;

    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    @JsonIgnore
    private LessonSection section;

    @PrePersist
    public void onCreate() {
        if (id == null) {
            id = "REF-" + UUID.randomUUID().toString().substring(0, 8);
        }
    }
}
