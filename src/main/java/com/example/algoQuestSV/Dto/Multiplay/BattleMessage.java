package com.example.algoQuestSV.Dto.Multiplay;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BattleMessage {
    private String type;      // JOIN, LEAVE, INVITE, CONFIG, KICK, CHANGE_TEAM, START_REQUEST
    private String sender;    // Tên người gửi
    private String content;   // Tên người bị mời/đuổi hoặc nội dung tin nhắn
    private String roomId;
    private String level;     // EASY, MEDIUM, HARD
    private String team;      // A hoặc B
    private Map<String, Object> additionalData; // Chứa danh sách players cập nhật
}
