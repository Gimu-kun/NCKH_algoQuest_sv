package com.example.algoQuestSV.Dto.Challenge;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class QuestionDetail {
    private String type;
    private Integer topicIndex;
    private int count;
}
