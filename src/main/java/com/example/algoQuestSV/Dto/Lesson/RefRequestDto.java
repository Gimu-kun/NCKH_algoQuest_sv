package com.example.algoQuestSV.Dto.Lesson;

import com.example.algoQuestSV.Enum.RefType;
import lombok.Data;

@Data
public class RefRequestDto {
    private RefType type;
    private String url;
}
