package com.example.algoQuestSV.Entity;

import com.example.algoQuestSV.Enum.VisualizationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "visualizations")
public class Visualization {
    @Id
    private String id;

    @Column(name = "visualization_type")
    private VisualizationType VisualizationType;

    private String Data;

    @Column(name = "template_code")
    private String TemplateCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", referencedColumnName = "id")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", referencedColumnName = "id")
    private User updatedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
