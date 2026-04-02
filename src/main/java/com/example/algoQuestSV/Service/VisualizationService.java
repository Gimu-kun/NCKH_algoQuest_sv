package com.example.algoQuestSV.Service;

import com.example.algoQuestSV.Dto.Api.ApiResponseDto;
import com.example.algoQuestSV.Dto.Visualization.*;
import com.example.algoQuestSV.Entity.*;
import com.example.algoQuestSV.Enum.VisualizationType;
import com.example.algoQuestSV.Repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.System.*;

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
            out.println("Lần đầu : "+ isFirstTime);
            if (isFirstTime) {
                out.println("Điểm User: "+ user.getPoint());
                out.println("Điểm QV: "+ qv.getPoint());
                user.setPoint(user.getPoint() + qv.getPoint());
                out.println("KN User: "+ user.getExp());
                out.println("KN QV: "+ qv.getExp());
                user.setExp(user.getExp() + qv.getExp());
                out.println("Vàng User: "+ user.getGold());
                out.println("Vàng QV: "+ qv.getGold());
                user.setGold(user.getGold() + qv.getGold());
                out.println("Đá User: "+ user.getStones());
                out.println("Đá QV: "+ qv.getStone());
                user.setStones(user.getStones() + qv.getStone());
                out.println("Gỗ User: "+ user.getWoods());
                out.println("Gỗ QV: "+ qv.getWood());
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

    @Transactional
    public ApiResponseDto<User> submitCodeChallenge(VisualCodeSubmitRequestDto dto) {
        try {
            // 1. Lấy thông tin & Chuẩn bị code
            QuestVisualization qv = questVisualRepo.findByQuestIdAndVisualizationId(dto.getQuestId(), dto.getVisualizationId()).orElseThrow();
            Visualization v = qv.getVisualization();
            JsonNode config = objectMapper.readTree(v.getData());
            String fullCode = v.getTemplateCode().replace("// user_code_here", dto.getUserCode()) + "\n" + config.get("test_cases").asText();

            // 2. Gọi Server C++
            CodeExecutionResponse response = callCppServer(fullCode, config);

            // 3. Parser kết quả (Dù PASS hay FAIL đều cần parse để lấy testDetails)
            if ("ok".equals(response.getStatus())) {
                List<TestCaseResult> details = parseGTestOutput(response.getRawOutput());
                response.setTestDetails(details);
                response.setTotalTests(details.size());
                response.setPassedTests((int) details.stream().filter(t -> "PASSED".equals(t.getStatus())).count());
            }

            // 4. LUÔN TRẢ VỀ 200 NẾU CPP SERVER CÓ PHẢN HỒI
            User user = usersRepository.findById(dto.getUserId()).orElseThrow();
            String message = "Thực thi hoàn tất";
            if ("compile_error".equals(response.getStatus())) message = "Lỗi biên dịch";
            else if (response.isAllTestsPassed()) {
                QuestProgress progress = progressRepository.findByUserIdAndQuestId(user.getId(), qv.getQuest().getId())
                        .orElse(null);
                boolean isFirstTime = (progress == null || !progress.getIsCompleted());
                if (isFirstTime) {
                    // Cộng các chỉ số tài nguyên
                    user.setPoint(user.getPoint() + qv.getPoint());
                    user.setExp(user.getExp() + qv.getExp());
                    user.setGold(user.getGold() + qv.getGold());
                    user.setStones(user.getStones() + qv.getStone());
                    user.setWoods(user.getWoods() + qv.getWood());
                    usersRepository.save(user);
                    message = "Tuyệt vời! Bạn đã vượt qua thử thách và nhận được phần thưởng.";
                } else {
                    message = "Thành công! (Bạn đã hoàn thành bài tập này trước đó).";
                }
                saveProgress(user, qv.getQuest(), qv, true);
            } else {
                saveProgress(user, qv.getQuest(), qv, false);
                message = "Thuật toán chưa chính xác. Một số test case bị thất bại.";
            }

            return ApiResponseDto.<User>builder()
                    .status(200)
                    .message(message)
                    .data(user)
                    .additionalData(response)
                    .build();

        } catch (Exception e) {
            return ApiResponseDto.<User>builder()
                    .status(500)
                    .message("Lỗi hệ thống: " + e.getMessage())
                    .build();
        }
    }

    private List<TestCaseResult> parseGTestOutput(String output) {
        List<TestCaseResult> results = new ArrayList<>();
        // Regex bắt: [  OK  ] AlgoTest.Case_n10_Expect_55 (0 ms)
        Pattern pattern = Pattern.compile("\\[\\s+(OK|FAILED)\\s+\\]\\s+AlgoTest\\.Case_n(\\d+)_Expect_(\\d+)\\s+\\((\\d+)\\s+ms\\)");
        Matcher matcher = pattern.matcher(output);

        while (matcher.find()) {
            String status = matcher.group(1).equals("OK") ? "PASSED" : "FAILED";
            String inputVal = matcher.group(2); // Lấy được 10
            String expectedVal = matcher.group(3); // Lấy được 55
            long timeMs = Long.parseLong(matcher.group(4));

            results.add(TestCaseResult.builder()
                    .name("Kiểm tra Fibonacci n = " + inputVal)
                    .input(inputVal)
                    .expected(expectedVal)
                    .status(status)
                    .durationMs((double) timeMs)
                    .cpuCycles(timeMs * 2800000L)
                    .build());
        }
        return results;
    }

    private CodeExecutionResponse callCppServer(String fullCode, JsonNode config) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8081/api/code_challenge_execute"; // Endpoint server C++ của bạn

        // Tạo body theo đúng định dạng JSON mà server.cpp đang đợi (json body = json::parse(req.body))
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("code", fullCode);
        requestBody.put("n", config.has("n_value") ? config.get("n_value").asInt() : 10);
        requestBody.put("limit", config.has("limit") ? config.get("limit").asLong() : 1000000L);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            out.println("Trước khi gọi");
            ResponseEntity<CodeExecutionResponse> response = restTemplate.postForEntity(url, entity, CodeExecutionResponse.class);
            System.out.println("Sau khi gọi");
            System.out.println(response);
            return response.getBody();
        } catch (Exception e) {
            CodeExecutionResponse errorRes = new CodeExecutionResponse();
            errorRes.setStatus("server_error");
            errorRes.setRawOutput("Không thể kết nối đến Execution Server: " + e.getMessage());
            return errorRes;
        }
    }
}
