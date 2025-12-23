package com.example.algoQuestSV.Dto.Quest;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class QuestContentAdjustDto {
    private List<BaseQuestContentDto> lessons;
    private List<BaseQuestContentDto> questions;
}
