package com.example.algoQuestSV.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "answers_mcq")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnswersMcq extends AnswerBase {
    @NotNull(message = "Thông tin câu trả lời không được để trống")
    private String content;
    @Column(name = "is_correct")
    private Boolean isCorrect = false;
}
