package com.example.algoQuestSV.Dto.Visualization;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CodeExecutionResponse {
    private String status;
    private Integer exitCode;
    private String rawOutput;

    // Các trường bổ sung để trả về cho Frontend
    private List<TestCaseResult> testDetails = new ArrayList<>();
    private int totalTests;
    private int passedTests;

    public boolean isAllTestsPassed() {
        return "ok".equalsIgnoreCase(status) && exitCode != null && exitCode == 0;
    }
}
