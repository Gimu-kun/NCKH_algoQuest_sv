package com.example.algoQuestSV.Service;

import com.example.algoQuestSV.Dto.Api.ApiResponseDto;
import com.example.algoQuestSV.Dto.Visualization.QuestVisualLinkRequestDto;
import com.example.algoQuestSV.Dto.Visualization.VisualBudgetSubmitRequestDto;
import com.example.algoQuestSV.Dto.Visualization.VisualSubmitRequestDto;
import com.example.algoQuestSV.Dto.Visualization.VisualizationRequestDto;
import com.example.algoQuestSV.Entity.*;
import com.example.algoQuestSV.Enum.VisualizationType;
import com.example.algoQuestSV.Repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VisualizationService {
    @Autowired
    private VisualizationsRepository visualRepo;
    @Autowired
    private QuestVisualizationRepository questVisualRepo;
    @Autowired
    private QuestsRepository questRepo;
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private QuestProgressRepository progressRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public Visualization createVisualization(VisualizationRequestDto dto) {
        Visualization v = Visualization.builder()
                .id(dto.getId())
                .visualizationType(VisualizationType.valueOf(dto.getVisualizationType()))
                .data(dto.getData())
                .templateCode(dto.getTemplateCode())
                .build();
        return visualRepo.save(v);
    }

    // Gắn Visualization vào một Quest cụ thể
    @Transactional
    public QuestVisualization linkToQuest(QuestVisualLinkRequestDto dto) {
        Quest quest = questRepo.findById(dto.getQuestId()).orElseThrow();
        Visualization visual = visualRepo.findById(dto.getVisualizationId()).orElseThrow();

        QuestVisualization qv = QuestVisualization.builder()
                .quest(quest)
                .visualization(visual)
                .point(dto.getPoint())
                .exp(dto.getExp())
                .gold(dto.getGold())
                .stone(dto.getStone())
                .wood(dto.getWood())
                .build();
        return questVisualRepo.save(qv);
    }

    // Lấy danh sách game của một Quest
    public List<QuestVisualization> getVisualsByQuest(String questId) {
        return questVisualRepo.findByQuestId(questId);
    }

    @Transactional
    public ApiResponseDto<User> submitVisualChallenge(VisualSubmitRequestDto dto) {
        // 1. Tìm bản ghi liên kết giữa Quest và Visualization
        QuestVisualization qv = questVisualRepo.findByQuestIdAndVisualizationId(dto.getQuestId(), dto.getVisualizationId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thử thách này trong ải!"));

        try {
            // 2. Parse dữ liệu cấu hình JSON từ cột data
            // Cấu trúc dự kiến: {"complexity_target": "O(n^2)", ...}
            JsonNode config = objectMapper.readTree(qv.getVisualization().getData());

            if (!config.has("target")) {
                throw new RuntimeException("Dữ liệu Visualization thiếu trường 'target'");
            }

            String correctAnswer = config.get("target").asText();

            // 4. Kiểm tra User
            User user = usersRepository.findById(dto.getUserId()).orElseThrow();

            // 4. Kiểm tra đáp án người dùng gửi lên
            if (!correctAnswer.equalsIgnoreCase(dto.getSelectedAnswer())) {
                saveProgress(user, qv.getQuest(), qv,false);
                return ApiResponseDto.<User>builder()
                        .status(400)
                        .message("Đáp án chưa chính xác. Hãy quan sát lại luồng chạy của thuật toán!")
                        .build();
            }

            // 5. Cộng thưởng
            QuestProgress progress = progressRepository.findByUserIdAndQuestId(user.getId(), qv.getQuest().getId())
                    .orElse(null);

            boolean isFirstTime = (progress == null || !progress.getIsCompleted());
            if (isFirstTime){
                user.setGold(user.getGold() + qv.getPoint());
                user.setExp(user.getExp() + qv.getExp());
                user.setPoint(user.getPoint() + qv.getPoint());
                user.setStones(user.getStones() + qv.getStone());
                user.setWoods(user.getWoods() + qv.getWood());
                usersRepository.save(user);
            }

            saveProgress(user, qv.getQuest(), qv,true);

            return ApiResponseDto.<User>builder()
                    .status(200)
                    .message("Chúc mừng! Bạn đã nắm vững độ phức tạp của thuật toán này.")
                    .data(user)
                    .build();

        } catch (Exception e) {
            return ApiResponseDto.<User>builder()
                    .status(500)
                    .message("Lỗi hệ thống khi xử lý dữ liệu: " + e.getMessage())
                    .build();
        }
    }

    @Transactional
    public ApiResponseDto<User> submitBudgetChallenge(VisualBudgetSubmitRequestDto dto) {
        // 1. Tìm bản ghi liên kết
        QuestVisualization qv = questVisualRepo.findByQuestIdAndVisualizationId(dto.getQuestId(), dto.getVisualizationId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thử thách!"));

        try {
            // 2. Parse Config từ JSON Data
            JsonNode config = objectMapper.readTree(qv.getVisualization().getData());
            int budgetLimit = config.get("budget_limit").asInt();

            // Tìm option người dùng đã chọn trong mảng options
            JsonNode selectedOption = null;
            for (JsonNode option : config.get("options")) {
                if (option.get("id").asText().equals(dto.getSelectedOptionId())) {
                    selectedOption = option;
                    break;
                }
            }

            if (selectedOption == null) throw new RuntimeException("Phương án không hợp lệ!");

            // 3. Kiểm tra điều kiện thắng (Logic kép)
            boolean isCorrectOption = selectedOption.get("correct").asBoolean();
            boolean isUnderBudget = dto.getActualSteps() <= budgetLimit;
            boolean isWin = isCorrectOption && isUnderBudget;

            // 4. Kiểm tra QuestProgress để xử lý thưởng
            User user = usersRepository.findById(dto.getUserId()).orElseThrow();

            if (!isWin) {
                saveProgress(user, qv.getQuest(), qv,false);
                return ApiResponseDto.<User>builder()
                        .status(400)
                        .message(isUnderBudget ? "Phương án chưa tối ưu!" : "Hệ thống quá tải (Vượt Budget)!")
                        .build();
            }

            QuestProgress progress = progressRepository.findByUserIdAndQuestId(user.getId(), qv.getQuest().getId())
                    .orElse(null);

            boolean isFirstTime = (progress == null || !progress.getIsCompleted());
            System.out.println("Lần đầu : "+ isFirstTime);
            if (isFirstTime) {
                System.out.println("Điểm User: "+ user.getPoint());
                System.out.println("Điểm QV: "+ qv.getPoint());
                user.setPoint(user.getPoint() + qv.getPoint());
                System.out.println("KN User: "+ user.getExp());
                System.out.println("KN QV: "+ qv.getExp());
                user.setExp(user.getExp() + qv.getExp());
                System.out.println("Vàng User: "+ user.getGold());
                System.out.println("Vàng QV: "+ qv.getGold());
                user.setGold(user.getGold() + qv.getGold());
                System.out.println("Đá User: "+ user.getStones());
                System.out.println("Đá QV: "+ qv.getStone());
                user.setStones(user.getStones() + qv.getStone());
                System.out.println("Gỗ User: "+ user.getWoods());
                System.out.println("Gỗ QV: "+ qv.getWood());
                user.setWoods(user.getWoods() + qv.getWood());
                usersRepository.save(user);
            }

            saveProgress(user, qv.getQuest(), qv,true);
            // 5. Trả về kết quả
            return ApiResponseDto.<User>builder()
                    .status(200)
                    .message(isFirstTime ? "Chúc mừng! Bạn nhận được phần thưởng lần đầu." : "Hoàn thành! (Bạn đã nhận thưởng trước đó)")
                    .data(user)
                    .build();

        } catch (Exception e) {
            return ApiResponseDto.<User>builder().status(500).message("Lỗi: " + e.getMessage()).build();
        }
    }

    private void saveProgress(User user, Quest quest, QuestVisualization qv, Boolean currentAttemptSuccess) {
        QuestProgress progress = progressRepository.findByUserIdAndQuestId(user.getId(), quest.getId())
                .orElse(new QuestProgress());

        if (progress.getIsCompleted() != null && progress.getIsCompleted()) {
            progress.setIsCompleted(true);
        } else {
            progress.setIsCompleted(currentAttemptSuccess);
        }

        progress.setUser(user);
        progress.setQuest(quest);
        progress.setCreatedAt(LocalDateTime.now());

        if (currentAttemptSuccess && (progress.getEarnedGold() == null || progress.getEarnedGold() == 0)) {
            progress.setEarnedGold(qv.getGold());
            progress.setEarnedExp(qv.getExp());
            progress.setEarnedPoint(qv.getPoint());
            progress.setEarnedStone(qv.getStone());
            progress.setEarnedWood(qv.getWood());
        }

        progressRepository.save(progress);
    }
}
