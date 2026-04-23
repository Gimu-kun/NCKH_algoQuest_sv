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
    private Map<String, List<Map<String, Object>>> submissionData = new ConcurrentHashMap<>();

    private Map<String, Object> getMockProblem() {
        Map<String, Object> problem = new HashMap<>();
        problem.put("title", "Tìm số lớn nhất trong mảng");
        problem.put("description", "Cho mảng số nguyên, trả về giá trị lớn nhất.");
        problem.put("template", "public int solution(int[] arr) {\n  // Code ở đây\n}");
        problem.put("testCases", List.of(
                Map.of("input", "[1, 5, 3]", "output", "5"),
                Map.of("input", "[-10, -5, 0]", "output", "0"),
                Map.of("input", "[-20, 20, 16]", "output", "20")
        ));
        return problem;
    }

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

        if ("START_REQUEST".equals(message.getType())) {
            message.setType("START_GAME");
            // Đính kèm đề thi vào tin nhắn
            message.setAdditionalData(Map.of(
                    "problem", getMockProblem(),
                    "startTime", System.currentTimeMillis()
            ));
        }

        if ("SUBMIT_CODE".equals(message.getType())) {
            List<Map<String, Object>> results = submissionData.computeIfAbsent(roomId, k -> new ArrayList<>());

            // Lưu kết quả của người gửi
            Map<String, Object> userResult = new HashMap<>(message.getAdditionalData());
            userResult.put("name", message.getSender());
            results.add(userResult);

            // Giả sử phòng có 2 người
            if (results.size() >= 2) {
                // Logic so sánh: Ai đúng nhiều hơn thắng, nếu bằng nhau ai làm nhanh hơn thắng
                // Ở đây ta có thể gắn flag isWinner cho người thắng
                message.setType("BATTLE_RESULT");
                message.setAdditionalData(Map.of("results", results));

                // Sau khi gửi xong có thể xóa dữ liệu phòng để giải phóng bộ nhớ
                submissionData.remove(roomId);
                return message; // Gửi đến tất cả mọi người trong kênh /topic/room/{roomId}
            } else {
                // Chỉ mới có 1 người nộp, chưa trả về tin nhắn chung hoặc trả về tin nhắn "đợi"
                return null;
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