package com.example.algoQuestSV.Entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

@Entity
@Data
@Builder
@Table(name = "question_imgs")
public class QuestionImgs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question questionId;

    private String url;

    @Column(name = "index_order")
    private Integer indexOrder;
}
