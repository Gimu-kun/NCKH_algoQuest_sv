package com.example.algoQuestSV.Dto.Quest;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BaseQuestContentDto {
    String id;
    Boolean defaultMark = false;
    Integer point;
    Integer exp;
    Integer gold;
    Integer stone;
    Integer wood;
}
