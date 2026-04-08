package com.example.algoQuestSV.Service;

import com.example.algoQuestSV.Dto.Api.ApiResponseDto;
import com.example.algoQuestSV.Dto.Challenge.CurrentChallengeStateDto;
import com.example.algoQuestSV.Entity.ChallengeSession;
import com.example.algoQuestSV.Entity.Stage;
import com.example.algoQuestSV.Entity.UserStageProgress;
import com.example.algoQuestSV.Enum.ChallengeStatus;
import com.example.algoQuestSV.Enum.StageType;
import com.example.algoQuestSV.Repository.ChallengeSessionRepository;
import com.example.algoQuestSV.Repository.StageRepository;
import com.example.algoQuestSV.Repository.UserStageProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ChallengeService {
    @Autowired
    ChallengeSessionRepository challengeSessionRepo;

    @Autowired
    UserStageProgressRepository stageProgressRepo;

    @Autowired
    StageRepository stageRepo;

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
        res.setCurrentStage(currentProgress.getStageNum());
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
}
