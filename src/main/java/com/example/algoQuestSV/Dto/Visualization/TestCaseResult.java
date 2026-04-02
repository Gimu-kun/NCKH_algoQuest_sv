package com.example.algoQuestSV.Dto.Visualization;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TestCaseResult {
    private String name;

    private String status;
    private Double durationMs;
    private Long cpuCycles;
    private String input;
    private String expected;
    private String message;
}
