package com.example.algoQuestSV.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "answers_fns")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnswersFns extends AnswerBase {
    @NotNull(message = "Thông tin câu trả lời không được để trống")
    private String answer;
}
