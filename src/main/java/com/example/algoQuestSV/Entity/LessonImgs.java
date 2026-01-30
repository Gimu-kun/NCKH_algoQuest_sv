package com.example.algoQuestSV.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LessonImgs {
    @Id
    private String id;

    @Column(name = "section_id")
    @NotNull(message = "ID mục bài học không được trống")
    private String sectionId;

    @NotNull(message = "Đường dẫn hình ảnh không được trống")
    private String url;

    @Column(name = "index_order")
    private Integer indexOrder;

    @PrePersist
    protected void onCreate() {
        if (id == null) id = "LI-" + UUID.randomUUID().toString().replace("-", "").substring(0,5);
    }
}
