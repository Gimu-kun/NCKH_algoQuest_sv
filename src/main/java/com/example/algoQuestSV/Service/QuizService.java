package com.example.algoQuestSV.Service;

import com.example.algoQuestSV.Dto.Quiz.*;
import com.example.algoQuestSV.Entity.*;
import com.example.algoQuestSV.Repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class QuizService {

    @Autowired
    QuestionsRepository questionRepo;

    @Autowired
    QuestsRepository questRepo;

    @Autowired
    UsersRepository userRepo;

    @Autowired
    AnswersMcqRepository mcqRepo;

    @Autowired
    AnswersMpRepository mpRepo;

    @Autowired
    AnswersFnRepository fnRepo;

    @Autowired
    AnswersFsRepository fsRepo;

    @Autowired
    AnswersFnsRepository fnsRepo;

    @Autowired
    QuestProgressRepository progressRepo;

    @Autowired
    QuestAnswerStorageRepository storageRepo;

    @Autowired
    ObjectMapper objectMapper;

    @Transactional
    public QuizResultResponse submitQuiz(QuizSubmissionRequest request) {
        // 1. Khởi tạo dữ liệu gốc
        Quest quest = questRepo.findById(request.getQuestId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ải"));
        User user = userRepo.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // 2. Lấy kỷ lục tốt nhất từ trước đến nay (Để tính chênh lệch)
        MaxRewardProjection maxRewards = progressRepo.findMaxRewardsByUserIdAndQuestId(user.getId(), quest.getId());

        int maxExp = (maxRewards != null && maxRewards.getMaxEarnedExp() != null) ? maxRewards.getMaxEarnedExp() : 0;
        int maxPoint = (maxRewards != null && maxRewards.getMaxEarnedPoint() != null) ? maxRewards.getMaxEarnedPoint() : 0;
        int maxGold = (maxRewards != null && maxRewards.getMaxEarnedGold() != null) ? maxRewards.getMaxEarnedGold() : 0;
        int maxStone = (maxRewards != null && maxRewards.getMaxEarnedStone() != null) ? maxRewards.getMaxEarnedStone() : 0;
        int maxWood = (maxRewards != null && maxRewards.getMaxEarnedWood() != null) ? maxRewards.getMaxEarnedWood() : 0;

        List<QuizResultDetail> details = new ArrayList<>();
        int correctCount = 0;

        // Tài nguyên đạt được CHỈ trong lượt chơi này
        int currentTotalExp = 0;
        int currentTotalPoint = 0;
        int currentTotalGold = 0;
        int currentTotalStone = 0;
        int currentTotalWood = 0;

        List<QuestQuestion> questQuestions = quest.getQuestions();

        // 3. Vòng lặp chấm điểm
        for (UserAnswerDto ansDto : request.getUserAnswer()) {
            boolean isCorrect = false;
            Object correctAnswer = null;
            String qId = ansDto.getQuestionId();
            String type = ansDto.getQuestionType().toUpperCase();

            // Logic chấm điểm (MCQ, MP, FN, FS, FNS)
            switch (type) {
                case "MCQ":
                    List<AnswersMcq> mcqAns = mcqRepo.findByQuestionId(qId);
                    AnswersMcq correctMcq = mcqAns.stream().filter(AnswersMcq::getIsCorrect).findFirst().orElse(null);
                    correctAnswer = correctMcq != null ? correctMcq.getId() : null;
                    isCorrect = ansDto.getSelectedMcqId() != null && ansDto.getSelectedMcqId().equals(correctAnswer);
                    break;
                case "MP":
                    List<AnswersMp> mpAns = mpRepo.findByQuestionId(qId);
                    Map<String, String> userMatches = ansDto.getMpMatches();
                    if (userMatches != null && userMatches.size() == mpAns.size()) {
                        isCorrect = userMatches.entrySet().stream().allMatch(e -> e.getKey().equals(e.getValue()));
                    }
                    correctAnswer = "Match IDs";
                    break;
                case "FN":
                    AnswersFn fn = fnRepo.findByQuestionId(qId).getFirst();
                    correctAnswer = fn.getAnswer();
                    if (ansDto.getTextAnswer() != null && !ansDto.getTextAnswer().isEmpty()) {
                        try {
                            double userVal = Double.parseDouble(ansDto.getTextAnswer());
                            isCorrect = Math.abs(userVal - fn.getAnswer()) <= fn.getTolerance();
                        } catch (NumberFormatException e) { isCorrect = false; }
                    }
                    break;
                case "FS":
                    AnswersFs fs = fsRepo.findByQuestionId(qId).getFirst();
                    correctAnswer = fs.getAnswer();
                    isCorrect = ansDto.getTextAnswer() != null && ansDto.getTextAnswer().trim().equalsIgnoreCase(fs.getAnswer().trim());
                    break;
                case "FNS":
                    AnswersFns fns = fnsRepo.findByQuestionId(qId).getFirst();
                    correctAnswer = fns.getAnswer();
                    isCorrect = ansDto.getTextAnswer() != null && ansDto.getTextAnswer().trim().equals(fns.getAnswer().trim());
                    break;
            }

            // Tính điểm thưởng cho câu này nếu đúng
            QuestQuestion qq = questQuestions.stream()
                    .filter(q -> q.getQuestion().getId().equals(qId))
                    .findFirst().orElse(null);

            if (isCorrect) {
                correctCount++;
                if (qq != null) {
                    currentTotalExp += (qq.getExp() != null ? qq.getExp().intValue() : 0);
                    currentTotalPoint += (qq.getPoint() != null ? qq.getPoint().intValue() : 0);
                    currentTotalGold += (qq.getGold() != null ? qq.getGold() : 0);
                    currentTotalStone += (qq.getStone() != null ? qq.getStone() : 0);
                    currentTotalWood += (qq.getWood() != null ? qq.getWood() : 0);
                }
            }

            details.add(QuizResultDetail.builder()
                    .questionId(qId)
                    .isCorrect(isCorrect)
                    .correctAnswer(correctAnswer)
                    .earnedExp(!isCorrect ? 0 : qq.getExp())
                    .earnedPoint(!isCorrect ? 0 : qq.getPoint())
                    .earnedGold(!isCorrect ? 0 : qq.getGold())
                    .earnedStone(!isCorrect ? 0 : qq.getStone())
                    .earnedWood(!isCorrect ? 0 : qq.getWood())
                    .userChoice(ansDto)
                    .build());
        }

        // 4. Kiểm tra điều kiện vượt qua (>= 50%)
        int totalQuestions = request.getUserAnswer().size();
        boolean isPassed = ((double) correctCount / totalQuestions) >= 0.5;

        // 5. Tính toán phần chênh lệch thực tế cộng vào ví User
        int diffExp = 0, diffPoint = 0, diffGold = 0, diffStone = 0, diffWood = 0;

        if (isPassed) {
            diffExp = Math.max(0, currentTotalExp - maxExp);
            diffPoint = Math.max(0, currentTotalPoint - maxPoint);
            diffGold = Math.max(0, currentTotalGold - maxGold);
            diffStone = Math.max(0, currentTotalStone - maxStone);
            diffWood = Math.max(0, currentTotalWood - maxWood);

            // Cập nhật ví tài nguyên của người chơi
            user.setExp(user.getExp() + diffExp);
            user.setPoint(user.getPoint() + diffPoint);
            user.setGold(user.getGold() + diffGold);
            user.setStones(user.getStones() + diffStone);
            user.setWoods(user.getWoods() + diffWood);
            userRepo.save(user);
        }

        // 6. Tạo record QuestProgress mới (Lưu kết quả lượt này)
        QuestProgress progress = QuestProgress.builder()
                .quest(quest)
                .user(user)
                .isCompleted(isPassed)
                .earnedExp(diffExp)
                .earnedPoint(diffPoint)
                .earnedGold(diffGold)
                .earnedStone(diffStone)
                .earnedWood(diffWood)
                .build();
        progress = progressRepo.save(progress);

        // 7. Lưu snapshot bài làm vào Storage
        try {
            storageRepo.save(QuestAnswerStorage.builder()
                    .questProgress(progress)
                    .answerData(objectMapper.writeValueAsString(request.getUserAnswer()))
                    .resultDetail(objectMapper.writeValueAsString(details))
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Lỗi lưu snapshot bài làm: " + e.getMessage());
        }

        // 8. Trả kết quả về Frontend
        return QuizResultResponse.builder()
                .correctCount(correctCount)
                .totalCount(totalQuestions)
                .isPassed(isPassed)
                .preMaxExp(maxExp)
                .earnedExp(diffExp)
                .preMaxPoint(maxPoint)
                .earnedPoint(diffPoint)
                .preMaxGold(maxGold)
                .earnedGold(diffGold)
                .preMaxStone(maxStone)
                .earnedStone(diffStone)
                .preMaxWood(maxWood)
                .earnedWood(diffWood)
                .user(user)           // Trả về thông tin User đã cập nhật ví
                .details(details)
                .build();
    }
}