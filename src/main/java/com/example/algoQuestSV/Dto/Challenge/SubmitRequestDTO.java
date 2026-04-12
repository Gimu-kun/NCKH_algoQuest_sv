package com.example.algoQuestSV.Dto.Challenge;

import lombok.Data;

import java.util.List;

@Data
public class SubmitRequestDTO {
    private Long progressId;
    private Long sessionId;
    private List<SubmitAnswerDTO> answers;
}
