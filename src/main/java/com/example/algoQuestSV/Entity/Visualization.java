package com.example.algoQuestSV.Entity;

import com.example.algoQuestSV.Enum.VisualizationType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "visualizations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Visualization {

    @Id
    @Column(columnDefinition = "bpchar(8)")
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "visualization_type")
    private VisualizationType visualizationType;

    @Column(columnDefinition = "text")
    private String data; // Chứa cấu hình JSON cho game (n, target, complexity...)

    @Column(name = "template_code", columnDefinition = "text")
    private String templateCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}