package com.example.algoQuestSV.Dto.Topic;

import lombok.Data;

@Data
public class TopicUpdateDto{
    private String title;

    private String description;

    private Integer indexOrder;

    private Boolean status;

    private String operatorId;
}
