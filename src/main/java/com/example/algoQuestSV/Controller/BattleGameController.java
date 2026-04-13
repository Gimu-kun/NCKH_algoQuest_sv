package com.example.algoQuestSV.Controller;

import com.example.algoQuestSV.Dto.Multiplay.BattleMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Controller
public class BattleGameController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private static final Map<String, List<Map<String, Object>>> lobbyData = new ConcurrentHashMap<>();

    // Xử lý các sự kiện chung trong phòng (Join, Config, Đổi đội, Start)
    @MessageMapping("/battle/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public BattleMessage handleBattleEvents(@DestinationVariable String roomId, @Payload BattleMessage message) {
        // Lấy hoặc tạo mới danh sách người chơi cho phòng này
        List<Map<String, Object>> players = lobbyData.computeIfAbsent(roomId, k -> new CopyOnWriteArrayList<>());

        if ("JOIN".equals(message.getType())) {
            // Kiểm tra xem người này đã có trong danh sách chưa (tránh trùng lặp khi F5)
            boolean exists = players.stream().anyMatch(p -> p.get("name").equals(message.getSender()));

            if (!exists) {
                Map<String, Object> newPlayer = new HashMap<>();
                newPlayer.put("id", UUID.randomUUID().toString()); // Hoặc ID thật từ DB
                newPlayer.put("name", message.getSender());
                newPlayer.put("status", "Ready");
                // Tự động xếp đội: người 1,3 đội A; người 2,4 đội B
                newPlayer.put("team", (players.size() % 2 == 0) ? "A" : "B");
                // Người đầu tiên vào phòng là chủ phòng
                newPlayer.put("isHost", players.isEmpty());

                players.add(newPlayer);
            }
        }

        // Quan trọng: Gán danh sách mới nhất vào additionalData để Frontend hiển thị
        message.setAdditionalData(Map.of("players", players));
        return message;
    }

    // Xử lý mời người chơi (Nhắn tin riêng)
    @MessageMapping("/invite")
    public void sendInvite(@Payload BattleMessage message) {
        // message.getContent() nên là username (hoặc ID duy nhất) của người được mời
        String targetUser = message.getContent();

        System.out.println("Gửi lời mời từ " + message.getSender() + " tới " + targetUser);

        // Gửi đến kênh /user/{targetUser}/queue/invites
        messagingTemplate.convertAndSendToUser(
                targetUser,
                "/queue/invites",
                message
        );
    }
}