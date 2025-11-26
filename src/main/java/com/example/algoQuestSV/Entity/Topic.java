package com.example.algoQuestSV.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "topics")
public class Topic {
    @Id
    private String id;

    @Length(min = 10, message = "Tiêu đề chương tối thiểu phải có 10 chữ cái")
    @NotNull(message = "Tiêu đề chương không được để trống!")
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

    @OneToMany(mappedBy = "topicId", cascade = CascadeType.ALL)
    private List<Quest> quests;

    @PrePersist
    protected void onCreate() {
        if (id == null) id = "T-" + UUID.randomUUID().toString().replace("-", "").trim().substring(0,5);
        if (createdAt == null) createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
