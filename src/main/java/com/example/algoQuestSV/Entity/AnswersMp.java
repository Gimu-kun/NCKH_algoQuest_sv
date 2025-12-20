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
@Table(name = "answers_mp")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnswersMp extends AnswerBase{
    @Column(name = "column_1")
    @NotNull(message = "Thông tin cột 1 không được để trống")
    private String column1;

    @Column(name = "column_2")
    @NotNull(message = "Thông tin cột 2 không được để trống")
    private String column2;
}
