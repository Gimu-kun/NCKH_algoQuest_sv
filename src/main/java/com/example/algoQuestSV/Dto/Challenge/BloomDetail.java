package com.example.algoQuestSV.Dto.Challenge;

import com.example.algoQuestSV.Dto.Challenge.QuestionDetail;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class BloomDetail {
    private double weight;
    private List<QuestionDetail> questions;
}