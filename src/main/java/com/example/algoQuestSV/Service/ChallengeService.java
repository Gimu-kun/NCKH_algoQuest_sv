package com.example.algoQuestSV.Service;

import com.example.algoQuestSV.Dto.Api.ApiResponseDto;
import com.example.algoQuestSV.Dto.Challenge.*;
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
            currentProgress = createNewProgress(currentSession.getId() , 1);
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
            currentProgress = stageProgressRepo.findAllBySessionIdAndStatusOrderByIdDesc(currentSession.getId(), ChallengeStatus.PENDING);

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

    private UserStageProgress createNewProgress(Long sessionId, int stageNum) {
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
        newProgress.setRemainLife(5);
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
        Visualization v = visualizationsRepo.findRandomByDifficultyRange(
                phrase.getMinDiff(),
                phrase.getMaxDiff(),
                safeExclude
        );

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
                    UserStageVisualization stageVisual = userStageVisualizationRepo.findAllByProgressId(p.getId());
                    usedActId.add(stageVisual.getVisualizationId());
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
}
