package com.example.algoQuestSV.Dto.User;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserGeneralDto {
    private String id;
    private String username;
    private String firstname;
    private String lastname;
    private Integer level;
    private Integer exp;
    private Integer wood;
    private Integer stone;
    private Integer point;
    private Integer gold;
    private Boolean role;
    private String avatar;
}
