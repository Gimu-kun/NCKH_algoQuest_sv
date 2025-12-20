package com.example.algoQuestSV.Dto.User;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserGeneralDto {
    private String id;
    private String username;
    private String fullname;
    private Boolean role;
}
