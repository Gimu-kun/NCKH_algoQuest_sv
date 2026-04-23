package com.example.algoQuestSV.Service;

import com.example.algoQuestSV.Dto.Api.ApiResponseDto;
import com.example.algoQuestSV.Dto.Challenge.*;
import com.example.algoQuestSV.Dto.Visualization.VisualizationSubmitDto;
import com.example.algoQuestSV.Entity.*;
import com.example.algoQuestSV.Enum.BloomType;
import com.example.algoQuestSV.Enum.ChallengeStatus;
import com.example.algoQuestSV.Enum.QuestionType;
import com.example.algoQuestSV.Enum.StageType;
import com.example.algoQuestSV.Repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class ChallengeService {
    @Autowired
    ChallengeSessionRepository challengeSessionRepo;

    @Autowired
    UserStageProgressRepository stageProgressRepo;

    @Autowired
    TopicsRepository topicRepo;

    @Autowired
    StageRepository stageRepo;

    @Autowired
    PhraseRepository phraseRepo;

    @Autowired
    QuestionsRepository questionsRepo;

    @Autowired
    VisualizationsRepository visualizationsRepo;

    @Autowired
    UserStageQuestionsRepository userStageQuestionsRepo;

    @Autowired
    UserStageVisualizationRepository userStageVisualizationRepo;

    @Autowired
    UserStageProgressRepository userStageProgressRepo;

    @Autowired
    UsersRepository userRepo;

    // Quy đổi Bloom sang trọng số
    private int getBloomWeight(BloomType type) {
        if (type == null) return 1;
        return switch (type) {
            case r -> 1;  // Remember
            case u -> 2;  // Understand
            case ap -> 3; // Apply
            case an -> 4; // Analyze
            default -> 1;
        };
    }

    // Lấy trọng số của Phrase dựa trên phraseNum
    private int getPhraseWeight(Long phraseId) {
        return phraseRepo.findById(phraseId)
                .map(Phrase::getPhraseNum)
                .orElse(1); // Mặc định là 1 nếu không tìm thấy
    }

    public ApiResponseDto<List<StageGeneralType>> getStageList(){
        List<StageGeneralType> generalTypes = new ArrayList<>();
        List<Stage> stageList = stageRepo.findAllByOrderByStageNumAsc();

        for (Stage s : stageList) {
            StageGeneralType stage = new StageGeneralType();

            stage.setStageNum(s.getStageNum());
            stage.setDifficulty(s.getDifficulty());
            stage.setStageType(s.getStageType());

            phraseRepo.findById(s.getPhraseId())
                    .ifPresent(phrase -> stage.setPhraseNum(phrase.getPhraseNum()));

            generalTypes.add(stage);
        }
        return ApiResponseDto.<List<StageGeneralType>>builder()
                .status(200)
                .message("Lấy danh sách tổng quan ải thành công")
                .data(generalTypes)
                .build();
    }

    @Transactional
    public ApiResponseDto<CurrentChallengeStateDto> getSessionState(String userId) {
        // 1. Tìm tất cả session đang PENDING của user
        List<ChallengeSession> pendingList = challengeSessionRepo.findAllByUserIdAndStatusOrderByIdDesc(userId, ChallengeStatus.PENDING);
        ChallengeSession currentSession;
        UserStageProgress currentProgress;

        // Nếu chưa có phiên chơi
        if (pendingList.isEmpty()) {
            // Tạo phiên chơi thử thách mới
            ChallengeSession newSession = createNewSession(userId);
            currentSession = challengeSessionRepo.save(newSession);
            // Tạo màn đầu tiên cho phiên chơi
            currentProgress = createNewProgress(currentSession.getId() , 1, 5);
        } else {
            // Lấy phiên chơi mới nhất
            currentSession = pendingList.getFirst();

            // Nếu có từ 2 cái trở lên, "dọn dẹp" các cái cũ
            if (pendingList.size() > 1) {
                for (int i = 1; i < pendingList.size(); i++) {
                    ChallengeSession oldSession = pendingList.get(i);
                    oldSession.setStatus(ChallengeStatus.COMPLETED);
                    oldSession.setCompletedAt(LocalDateTime.now());
                }
                challengeSessionRepo.saveAll(pendingList.subList(1, pendingList.size()));
            }

            // Lấy màn chơi hiện hành trong phiên chơi đã tìm thấy
            System.out.println("ID: "+ currentSession.getId());
            currentProgress = stageProgressRepo.findAllBySessionIdAndStatusOrderByIdDesc(currentSession.getId(), ChallengeStatus.PENDING);
            System.out.println("currentProgress: "+ currentProgress);
        }

        CurrentChallengeStateDto res = new CurrentChallengeStateDto();
        assert currentProgress != null;
        res.setCurrentStage(currentProgress.getStageNum());
        res.setSessionId(currentProgress.getSessionId());
        res.setProgressId(currentProgress.getId());
        res.setUserId(currentSession.getUserId());
        res.setRemainLife(currentProgress.getRemainLife());

        // 3. Map sang DTO và trả về
        return ApiResponseDto.<CurrentChallengeStateDto>builder()
                .status(200)
                .message("Truy xuất thành công")
                .data(res)
                .build();
    }

    private ChallengeSession createNewSession(String userId){
        ChallengeSession newSession = new ChallengeSession();
        newSession.setUserId(userId);
        newSession.setStartedAt(LocalDateTime.now());
        newSession.setStatus(ChallengeStatus.PENDING);
        return newSession;
    }

    private UserStageProgress createNewProgress(Long sessionId, int stageNum , int remainLife) {
        Optional<Stage> stage = stageRepo.findByStageNum(stageNum);
        if(stage.isEmpty()){
            return null;
        }

        UserStageProgress newProgress = new UserStageProgress();
        newProgress.setSessionId(sessionId);
        newProgress.setStageNum(stageNum);
        newProgress.setStageType(stage.get().getStageType());
        newProgress.setStatus(ChallengeStatus.PENDING);
        newProgress.setStartedAt(LocalDateTime.now());
        newProgress.setRemainLife(remainLife);
        return stageProgressRepo.save(newProgress);
    }

    private List<Question> selectChallengeActivity(
            Stage stage,
            Phrase phrase,
            List<String> usedActId,
            Long progressId
    ) throws JsonProcessingException {

        if (stage.getStageType() != StageType.Q) {
            return Collections.emptyList();
        }

        List<UserStageQuestions> pendingQuestions = userStageQuestionsRepo.findAllByProgressId(progressId)
                .stream()
                .filter(q -> q.getIsAnswered() == null || !q.getIsAnswered())
                .toList();

        if (!pendingQuestions.isEmpty()) {
            // Nếu có câu hỏi Pending, trả về các câu hỏi đó
            List<Question> existingQuestions = new ArrayList<>();
            for (UserStageQuestions uq : pendingQuestions) {
                questionsRepo.findById(uq.getQuestionId()).ifPresent(existingQuestions::add);
            }
            return existingQuestions;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, BloomDetail> bloomMap = objectMapper.readValue(
                phrase.getBloomDistribution(),
                new TypeReference<>() {}
        );

        List<Question> quesList = new ArrayList<>();
        Set<String> usedIdsLocal = new HashSet<>(usedActId);

        for (Map.Entry<String, BloomDetail> bloomEntry : bloomMap.entrySet()) {
            BloomType bloomType = BloomType.valueOf(bloomEntry.getKey());
            BloomDetail detail = bloomEntry.getValue();

            for (QuestionDetail qd : detail.getQuestions()) {

                Optional<Topic> topicOpt = topicRepo.findByIndexOrder(qd.getTopicIndex());

                if (topicOpt.isEmpty()) continue;

                List<Question> allQuestions = questionsRepo.findAllWithFilter(
                        bloomType,
                        QuestionType.valueOf(qd.getType()),
                        topicOpt.get().getId(),
                        usedIdsLocal.isEmpty() ? null : new ArrayList<>(usedIdsLocal)
                );

                Collections.shuffle(allQuestions);

                int count = qd.getCount();

                for (Question q : allQuestions) {
                    if (!usedIdsLocal.contains(q.getId())) {
                        quesList.add(q);
                        usedIdsLocal.add(q.getId());

                        if (--count <= 0) break;
                    }
                }
            }
        }

        // save mapping
        for (Question q : quesList) {
            userStageQuestionsRepo.save(
                    UserStageQuestions.builder()
                            .questionId(q.getId())
                            .progressId(progressId)
                            .build()
            );
        }

        return quesList;
    }

    private Visualization selectChallengeVisualization(
            Stage stage,
            Phrase phrase,
            List<String> usedActId,
            Long progressId
    ) {
        List<String> safeExclude = usedActId.isEmpty() ? null : usedActId;
        Visualization v;
        Integer difficultyWeight = (phrase.getPhraseNum() == 1 || phrase.getPhraseNum() == 2) ? 1
                : (phrase.getPhraseNum() == 3 || phrase.getPhraseNum() == 4) ? 2
                : 3;
        if (safeExclude == null) {
            v = visualizationsRepo.findWithoutExclude(phrase.getMinDiff(), phrase.getMaxDiff(), difficultyWeight);
        } else {
            v = visualizationsRepo.findWithExclude(phrase.getMinDiff(), phrase.getMaxDiff(), difficultyWeight, safeExclude);
        }

        if (v != null) {
            userStageVisualizationRepo.save(
                    UserStageVisualization.builder()
                            .visualizationId(v.getId())
                            .progressId(progressId)
                            .build()
            );
        }

        return v;
    }

    public ApiResponseDto<StageDetailType> challengeModeEscalation(Long sessionId) throws JsonProcessingException {
        // Tổng hợp các Act đã sử dụng trong phiên chơi
        UserStageProgress currentProgress = stageProgressRepo.findBySessionIdAndStatus(sessionId, ChallengeStatus.PENDING);
        List<UserStageProgress> progresses = stageProgressRepo.findAllBySessionId(sessionId);
        Optional<Stage> stageOpt = stageRepo.findByStageNum(currentProgress.getStageNum());
        List<String> usedActId = new ArrayList<>();
        for (UserStageProgress p: progresses){
            switch (p.getStageType()) {
                case StageType.Q:
                    List<UserStageQuestions> stageQuestions = userStageQuestionsRepo.findAllByProgressId(p.getId());
                    for (UserStageQuestions uq : stageQuestions) {
                        usedActId.add(uq.getQuestionId());
                    }
                    break;
                case StageType.V:
                    List<UserStageVisualization> stageVisual = userStageVisualizationRepo.findAllByProgressId(p.getId());
                    for (UserStageVisualization uv : stageVisual) {
                        usedActId.add(uv.getVisualizationId());
                    }

            }
        }

        if (stageOpt.isEmpty()) {
            return ApiResponseDto.<StageDetailType>builder()
                    .status(400)
                    .message("thứ tự màn chơi không hợp lệ!")
                    .data(null)
                    .build();
        }

        Stage stage = stageOpt.get();

        Optional<Phrase> phraseOpt = phraseRepo.findById(stage.getPhraseId());

        if (phraseOpt.isEmpty()) {
            return ApiResponseDto.<StageDetailType>builder()
                    .status(400)
                    .message("Không tìm thấy phase tương ứng!")
                    .data(null)
                    .build();
        }

        Phrase phrase = phraseOpt.get();
        List<Question> qList = new ArrayList<>();
        Visualization visualization = null;
        //Chọn Act cho màn chơi hiện tại

        switch (stage.getStageType()){
            case StageType.Q:
                List<UserStageQuestions> uqList= userStageQuestionsRepo.findAllByProgressId(currentProgress.getId());
                if(!uqList.isEmpty()){
                    for (UserStageQuestions uq : uqList){
                        Optional<Question> q = questionsRepo.findById(uq.getQuestionId());
                        if (q.isPresent()){
                            qList.add(q.get());
                        }else{
                            return ApiResponseDto.<StageDetailType>builder()
                                    .status(400)
                                    .message("Không thể truy xuất danh sách câu hỏi!" + uq.getQuestionId())
                                    .data(null)
                                    .build();
                        }
                    }
                    break;
                }
                qList = selectChallengeActivity(stage, phrase, usedActId, currentProgress.getId());
                break;
            case StageType.V:
                Optional<UserStageVisualization> OptUv = userStageVisualizationRepo.findByProgressId(currentProgress.getId());
                if(OptUv.isPresent()){
                    Optional<Visualization> v = visualizationsRepo.findById(OptUv.get().getVisualizationId());
                    if (v.isPresent()){
                        visualization = v.get();
                    }else{
                        return ApiResponseDto.<StageDetailType>builder()
                                .status(400)
                                .message("Không thể truy xuất hoạt động minh hoạ!" + OptUv.get().getVisualizationId())
                                .data(null)
                                .build();
                    }
                    break;
                }
                visualization = selectChallengeVisualization(stage, phrase, usedActId, currentProgress.getId());
                break;
            default:
                break;
        }

        StageDetailType stageDetailType = new StageDetailType();
        stageDetailType.setType(stage.getStageType());
        stageDetailType.setQuestions(qList);
        stageDetailType.setVisualization(visualization);
        stageDetailType.setProgressId(currentProgress.getId());
        stageDetailType.setRemainLife(currentProgress.getRemainLife());
        return ApiResponseDto.<StageDetailType>builder()
                .status(200)
                .message("Lấy thông tin màn chơi thành công!")
                .data(stageDetailType)
                .build();
    }

    private Object getCorrectAnswer(Question question) {
        String type = question.getQuestionType().name().toLowerCase();

        return switch (type) {
            case "mcq" -> question.getMcqAnswers().stream()
                    .filter(AnswersMcq::getIsCorrect)
                    .map(AnswersMcq::getId)
                    .collect(Collectors.toList());
            case "fs" -> question.getFsAnswers().stream()
                    .map(a -> {
                        List<String> all = new ArrayList<>();
                        all.add(a.getAnswer());
                        if (a.getSynonyms() != null)
                            all.addAll(Arrays.asList(a.getSynonyms().split(",")));
                        return all;
                    })
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            case "fn" -> question.getFnAnswers().stream()
                    .map(a -> Map.of(
                            "value", a.getAnswer(),
                            "tolerance", a.getTolerance() != null ? a.getTolerance() : 0
                    ))
                    .collect(Collectors.toList());
            case "mp" -> question.getMpAnswers().stream()
                    .collect(Collectors.toMap(
                            AnswersMp::getId,
                            AnswersMp::getColumn2
                    ));
            default -> null;
        };
    }

    private Object extractUserAnswer(SubmitAnswerDTO ans) {
        if (ans.getTextAnswer() != null) return ans.getTextAnswer();
        if (ans.getSelectedMcqId() != null) return ans.getSelectedMcqId();
        if (ans.getMpMatches() != null) return ans.getMpMatches();
        return null;
    }

    @Transactional
    public boolean updateUserStageQuestions(Long progressId, List<SubmitAnswerDTO> answers, Map<String, Boolean> checkResults) {
        LocalDateTime now = LocalDateTime.now();
        int correctCount = 0;
        boolean isDone = false;

        // Cập nhật UserStageQuestions
        for (SubmitAnswerDTO ans : answers) {
            UserStageQuestions record = userStageQuestionsRepo
                    .findByProgressIdAndQuestionId(progressId, ans.getQuestionId());

            boolean isCorrect = checkResults.getOrDefault(ans.getQuestionId(), false);
            if (isCorrect) correctCount++;

            if (record != null) {
                record.setUserAnswer(ans.getTextAnswer() != null ? ans.getTextAnswer() : ans.getSelectedMcqId());
                record.setIsCorrect(isCorrect);
                record.setIsAnswered(true);
                record.setAnsweredAt(now);
                userStageQuestionsRepo.save(record);
            } else {
                UserStageQuestions newRecord = UserStageQuestions.builder()
                        .progressId(progressId)
                        .questionId(ans.getQuestionId())
                        .userAnswer(ans.getTextAnswer() != null ? ans.getTextAnswer() : ans.getSelectedMcqId())
                        .isCorrect(isCorrect)
                        .isAnswered(true)
                        .answeredAt(now)
                        .build();
                userStageQuestionsRepo.save(newRecord);
            }
        }

        // ================= CẬP NHẬT UserStageProgress =================
        UserStageProgress progress = stageProgressRepo.findById(progressId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy progress"));

        List<UserStageQuestions> allQuestions = userStageQuestionsRepo.findAllByProgressId(progressId);
        boolean allAnswered = allQuestions.stream().allMatch(q -> Boolean.TRUE.equals(q.getIsAnswered()));

        if (allAnswered) {
            long total = allQuestions.size();
            long correct = allQuestions.stream().filter(q -> Boolean.TRUE.equals(q.getIsCorrect())).count();

            // PASS nếu đúng >50%
            boolean pass = correct > total / 2;

            if (pass) {
                // ================= LOGIC TÍNH ĐIỂM (EXP) =================
                // 1. Tìm stage và phrase để lấy trọng số
                Stage stage = stageRepo.findByStageNum(progress.getStageNum()).orElse(null);
                if (stage != null) {
                    int phraseWeight = getPhraseWeight(stage.getPhraseId());

                    // 2. Tính điểm dựa trên trung bình trọng số Bloom của các câu hỏi trong màn
                    double avgBloomWeight = allQuestions.stream()
                            .map(uq -> questionsRepo.findById(uq.getQuestionId()).orElse(null))
                            .filter(Objects::nonNull)
                            .mapToDouble(q -> getBloomWeight(q.getBloom()))
                            .average().orElse(1.0);

                    double utility = avgBloomWeight * phraseWeight;
                    double accuracy = (double) correct / total;

                    // Công thức: utility * tỉ lệ đúng * 20
                    int calculatedPoint = (int) Math.round(utility * accuracy * 20);
                    int calculatedResource = calculatedPoint * 2;
                    int calculatedExp = calculatedPoint * 5;

                    progress.setPoint(calculatedPoint);
                    progress.setGold(calculatedResource);
                    progress.setStone(calculatedResource);
                    progress.setWood(calculatedResource);
                    progress.setExp(calculatedExp);

                    ChallengeSession session = challengeSessionRepo.findById(progress.getSessionId())
                            .orElseThrow(() -> new RuntimeException("Session không tồn tại"));

                    String userId = session.getUserId();

                    // Tìm User và cộng dồn giá trị
                    userRepo.findById(userId).ifPresent(user -> {
                        long currentTotalPoint = user.getPoint() != null ? user.getPoint() : 0;
                        user.setPoint((int) currentTotalPoint + calculatedPoint);
                        long currentTotalExp = user.getExp() != null ? user.getExp() : 0;
                        user.setExp((int) currentTotalExp + calculatedExp);
                        long currentTotalGold = user.getGold() != null ? user.getGold() : 0;
                        user.setGold((int) currentTotalGold + calculatedResource);
                        long currentTotalStone = user.getStones() != null ? user.getStones() : 0;
                        user.setStones((int) currentTotalStone + calculatedResource);
                        long currentTotalWood = user.getWoods() != null ? user.getWoods() : 0;
                        user.setWoods((int) currentTotalWood + calculatedResource);
                        userRepo.save(user);
                    });
                }
            } else {
                // Giảm remainLife nếu không pass
                long remainLife = progress.getRemainLife() != null ? progress.getRemainLife() : 0;
                progress.setRemainLife((int) Math.max(remainLife - 1, 0));
                progress.setExp(0); // Không pass không có điểm
            }

            // Cập nhật progress hiện tại
            progress.setStatus(ChallengeStatus.COMPLETED);
            progress.setCompletedAt(now);
            progress.setPass(pass);

            stageProgressRepo.save(progress);

            // ================= CẬP NHẬT ChallengeSession =================
            ChallengeSession session = challengeSessionRepo.findById(progress.getSessionId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy session"));

            boolean isLastStage = progress.getStageNum() == 100;
            if (progress.getRemainLife() <= 0 || (isLastStage && pass)) {
                session.setStatus(ChallengeStatus.COMPLETED);
                session.setCompletedAt(now);
                challengeSessionRepo.save(session);
                isDone = true;
            }

            // ================= TẠO màn chơi tiếp theo nếu pass và chưa phải màn cuối =================
            if (!isLastStage && progress.getRemainLife() > 0) {
                System.out.println("Tạo Progress mới");
                createNewProgress(session.getId(), pass ? progress.getStageNum() + 1 : progress.getStageNum(), progress.getRemainLife());
            }
        }
        return isDone;
    }

    @Transactional
    public ApiResponseDto<Map<String, Object>> submit(SubmitRequestDTO request) {
        List<ResultDTO> results = new ArrayList<>();
        int correctCount = 0;
        Map<String, Boolean> checkResults = new HashMap<>();

        for (SubmitAnswerDTO ans : request.getAnswers()) {
            Question question = questionsRepo.findById(ans.getQuestionId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy câu hỏi"));

            boolean isCorrect = checkAnswer(question, ans);
            checkResults.put(ans.getQuestionId(), isCorrect);

            if (isCorrect) correctCount++;
            results.add(new ResultDTO(
                    question,
                    isCorrect,
                    ans.getSelectedMcqId() != null ? ans.getSelectedMcqId() : ans.getTextAnswer()
            ));
        }

        // Cập nhật bảng UserStageQuestions
        boolean isDone = updateUserStageQuestions(request.getProgressId(), request.getAnswers(), checkResults);
        UserStageProgress currentProgress = stageProgressRepo.findById(request.getProgressId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy progress"));

        Map<String, Object> response = sumaryData(currentProgress,isDone,results, request.getAnswers().size(),correctCount);

        return ApiResponseDto.<Map<String, Object>>builder()
                .status(200)
                .message("Tính điểm thành công")
                .data(response)
                .build();
    }

    private Map<String, Object> sumaryData(UserStageProgress currentProgress, boolean isDone, List<ResultDTO> results, Integer total, Integer correctCount){
        //Tổng kết thành tích
        List<UserStageProgress> spList = userStageProgressRepo.findAllBySessionId(currentProgress.getSessionId());
        int totalStagesPassed = 0;
        int totalVisualizationsPassed = 0;
        int sessionGold = 0;
        int sessionStone = 0;
        int sessionWood = 0;
        int sessionPoint = 0;
        int sessionExp = 0;

        long totalSessionCorrect = 0;
        long totalSessionQuestions = 0;

        for (UserStageProgress p : spList) {
            if (p.isPass()) {
                totalStagesPassed++;
                if (p.getStageType() == StageType.V) {
                    totalVisualizationsPassed++;
                }
            }

            // Cộng dồn tài nguyên
            sessionGold += (p.getGold() != null ? p.getGold() : 0);
            sessionStone += (p.getStone() != null ? p.getStone() : 0);
            sessionWood += (p.getWood() != null ? p.getWood() : 0);
            sessionPoint += (p.getPoint() != null ? p.getPoint() : 0);
            sessionExp += (p.getExp() != null ? p.getExp() : 0);

            // Tính toán tỉ lệ đúng cho các màn trắc nghiệm
            if (p.getStageType() == StageType.Q) {
                // Lưu ý: Cần fetch Questions từ Repo nếu fetch type là LAZY và không nằm trong Transaction
                List<UserStageQuestions> qList = userStageQuestionsRepo.findAllByProgressId(p.getId());
                totalSessionQuestions += qList.size();
                totalSessionCorrect += qList.stream().filter(q -> Boolean.TRUE.equals(q.getIsCorrect())).count();
            }
        }

        double accuracyRate = totalSessionQuestions > 0
                ? (double) totalSessionCorrect / totalSessionQuestions * 100
                : 0;

        Map<String, Object> response = new HashMap<>();
        response.put("total", total);
        response.put("correct", correctCount);
        response.put("results", results);
        response.put("isDone", isDone);

        // Dữ liệu tổng hợp (Summary)
        Map<String, Object> summary = new HashMap<>();
        summary.put("stagesPassed", totalStagesPassed);
        summary.put("pass", currentProgress.isPass()); // THÊM DÒNG NÀY
        summary.put("remainLife", currentProgress.getRemainLife());
        summary.put("visualizationsPassed", totalVisualizationsPassed);
        summary.put("accuracyRate", Math.round(accuracyRate * 100.0) / 100.0); // Làm tròn 2 chữ số
        summary.put("resources", Map.of(
                "gold", sessionGold,
                "stone", sessionStone,
                "wood", sessionWood,
                "point", sessionPoint,
                "exp", sessionExp
        ));

        response.put("summary", summary);
        return response;
    }

    // ================= CHECK LOGIC =================
    private boolean checkAnswer(Question question, SubmitAnswerDTO ans) {

        String type = question.getQuestionType().name().toLowerCase();

        switch (type) {

            // ===== MCQ =====
            case "mcq":
                return question.getMcqAnswers().stream()
                        .anyMatch(a ->
                                a.getId().trim().equalsIgnoreCase(ans.getSelectedMcqId().trim())
                                        && Boolean.TRUE.equals(a.getIsCorrect())
                        );

            // ===== FILL STRING =====
            case "fs":
                String user = normalize(ans.getTextAnswer());

                return question.getFsAnswers().stream().anyMatch(a -> {
                    String correct = normalize(a.getAnswer());

                    if (user.equals(correct)) return true;

                    if (a.getSynonyms() != null) {
                        return Arrays.stream(a.getSynonyms().split(","))
                                .map(this::normalize)
                                .anyMatch(user::equals);
                    }

                    return false;
                });

            // ===== FILL NUMBER =====
            case "fn":
                try {
                    double userVal = Double.parseDouble(ans.getTextAnswer());

                    return question.getFnAnswers().stream().anyMatch(a -> {
                        double correct = a.getAnswer();
                        double tol = a.getTolerance() != null ? a.getTolerance() : 0;

                        return Math.abs(userVal - correct) <= tol;
                    });

                } catch (Exception e) {
                    return false;
                }

                // ===== MATCH PAIR =====
            case "mp":
                Map<String, String> userMatches = ans.getMpMatches();
                if (userMatches == null) return false;

                Map<String, String> correctMap = question.getMpAnswers()
                        .stream()
                        .collect(Collectors.toMap(
                                AnswersMp::getId,
                                AnswersMp::getId // ID match với chính nó
                        ));

                return correctMap.entrySet().stream().allMatch(entry -> {
                    String leftId = entry.getKey();
                    String rightId = entry.getValue();

                    return rightId.equals(userMatches.get(leftId));
                });

            default:
                return false;
        }
    }

    private String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    @Transactional
    public ApiResponseDto<Map<String, Object>> submitVisualizationResult(Long progressId, VisualizationSubmitDto dto) {
        // 1. Lấy Progress hiện tại
        UserStageProgress currentProgress = stageProgressRepo.findById(progressId)
                .orElseThrow(() -> new RuntimeException("Progress không tồn tại"));
        UserStageVisualization stageVisualization = userStageVisualizationRepo.findByProgressIdAndVisualizationId(progressId, dto.getVisualizationId()).orElseThrow(() -> new RuntimeException("Visualization không tồn tại"));
        Visualization v = visualizationsRepo.findById(dto.getVisualizationId()).orElseThrow(() -> new RuntimeException("Visualization không tồn tại"));
        ChallengeSession session = challengeSessionRepo.findById(currentProgress.getSessionId())
                .orElseThrow(() -> new RuntimeException("Session không tồn tại"));
        String userId = session.getUserId();
        boolean isDone = false;

        //Cập nhật trạng thái stageVisualization
        stageVisualization.setIsPassed(dto.isCorrect());
        stageVisualization.setCorrectOp(dto.getCorrectOp());
        stageVisualization.setIncorrectOp(dto.getIncorrectOp());
        stageVisualization.setSelectedAnswer(dto.getSelectedAnswer());
        userStageVisualizationRepo.save(stageVisualization);

        //Tính điểm và tài nguyên và cập nhật thông số qua màn của visualization
        int total = 0;
        if (dto.isCorrect()) {
            Stage stage = stageRepo.findByStageNum(currentProgress.getStageNum()).orElseThrow(() -> new RuntimeException("Stage không tồn tại"));
            int phraseWeight = getPhraseWeight(stage.getPhraseId());
            double utility = dto.getDifficulty() * phraseWeight;
            double accuracy = 1;
            total = 5;
            if (dto.getDifficulty() == 2) {
                accuracy = 5 - (double) dto.getIncorrectOp() / total;
            }

            int calculatedPoint = (int) Math.round(utility * accuracy * 10);
            int calculatedResource = calculatedPoint * 2;
            int calculatedExp = calculatedPoint * 5;

            currentProgress.setPoint(calculatedPoint);
            currentProgress.setGold(calculatedResource);
            currentProgress.setStone(calculatedResource);
            currentProgress.setWood(calculatedResource);
            currentProgress.setExp(calculatedExp);

            userRepo.findById(userId).ifPresent(user -> {
                long currentTotalPoint = user.getPoint() != null ? user.getPoint() : 0;
                user.setPoint((int) currentTotalPoint + calculatedPoint);
                long currentTotalExp = user.getExp() != null ? user.getExp() : 0;
                user.setExp((int) currentTotalExp + calculatedExp);
                long currentTotalGold = user.getGold() != null ? user.getGold() : 0;
                user.setGold((int) currentTotalGold + calculatedResource);
                long currentTotalStone = user.getStones() != null ? user.getStones() : 0;
                user.setStones((int) currentTotalStone + calculatedResource);
                long currentTotalWood = user.getWoods() != null ? user.getWoods() : 0;
                user.setWoods((int) currentTotalWood + calculatedResource);
                userRepo.save(user);
            });
        } else {
            long remainLife = currentProgress.getRemainLife() != null ? currentProgress.getRemainLife() : 0;
            currentProgress.setRemainLife((int) Math.max(remainLife - 1, 0));
            currentProgress.setExp(0);
        }

        currentProgress.setStatus(ChallengeStatus.COMPLETED);
        currentProgress.setCompletedAt(LocalDateTime.now());
        currentProgress.setPass(dto.isCorrect());

        stageProgressRepo.save(currentProgress);

        boolean isLastStage = currentProgress.getStageNum() == 100;
        if (currentProgress.getRemainLife() <= 0 || (isLastStage && dto.isCorrect())) {
            session.setStatus(ChallengeStatus.COMPLETED);
            session.setCompletedAt(LocalDateTime.now());
            challengeSessionRepo.save(session);
            isDone = true;
        }

        // ================= TẠO màn chơi tiếp theo nếu pass và chưa phải màn cuối =================
        if (!isLastStage && currentProgress.getRemainLife() > 0) {
            System.out.println("Tạo Progress mới");
            createNewProgress(session.getId(), dto.isCorrect() ? currentProgress.getStageNum() + 1 : currentProgress.getStageNum(), currentProgress.getRemainLife());
        }

        Map<String, Object> response = sumaryData(currentProgress, isDone, new ArrayList<>(), total, dto.getCorrectOp());

        return ApiResponseDto.<Map<String, Object>>builder()
                .status(200)
                .message("Tính điểm thành công")
                .data(response)
                .build();
    }
}
