package com.example.algoQuestSV.Dto.User;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserGeneralDto {
    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private Integer level;
    private Integer exp;
    private Integer woods;
    private Integer stones;
    private Integer point;
    private Integer gold;
    private Boolean role;
    private String avatar;
}
