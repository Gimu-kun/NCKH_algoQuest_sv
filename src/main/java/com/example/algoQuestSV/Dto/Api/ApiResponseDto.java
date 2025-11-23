package com.example.algoQuestSV.Dto.Api;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiResponseDto<T> {
    private Integer status;
    private String message;
    private T data;
}
