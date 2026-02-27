package com.example.algoQuestSV.Service;

import com.example.algoQuestSV.Dto.Api.ApiResponseDto;
import com.example.algoQuestSV.Dto.Quest.QuestRewardRequestDto;
import com.example.algoQuestSV.Entity.Quest;
import com.example.algoQuestSV.Entity.QuestProgress;
import com.example.algoQuestSV.Entity.QuestReward;
import com.example.algoQuestSV.Entity.User;
import com.example.algoQuestSV.Enum.QuestType;
import com.example.algoQuestSV.Repository.QuestProgressRepository;
import com.example.algoQuestSV.Repository.QuestRewardRepository;
import com.example.algoQuestSV.Repository.QuestsRepository;
import com.example.algoQuestSV.Repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RewardService {
    @Autowired
    private UsersRepository userRepository;
    @Autowired
    private QuestsRepository questRepository;
    @Autowired
    private QuestProgressRepository progressRepository;
    @Autowired
    private QuestRewardRepository rewardRepository;

    @Transactional
    public ApiResponseDto<User> claimBonusReward(String questId, String userId) {
        // 1. Kiểm tra Quest
        Quest quest = questRepository.findById(questId)
                .orElseThrow(() -> new RuntimeException("Ải không tồn tại"));

        if (quest.getType() != QuestType.reward) {
            return ApiResponseDto.<User>builder().status(400).message("Đây không phải là ải phần thưởng").build();
        }

        // 2. Kiểm tra xem đã hoàn thành chưa (đã nhận quà chưa)
        boolean alreadyClaimed = progressRepository.findByUserIdAndQuestId(userId, questId)
                .map(QuestProgress::getIsCompleted)
                .orElse(false);

        if (alreadyClaimed) {
            return ApiResponseDto.<User>builder().status(400).message("Bạn đã nhận phần thưởng này rồi").build();
        }

        // 3. Lấy User và cộng quà
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        for (QuestReward reward : quest.getRewards()) {
            user.setExp(user.getExp() + (reward.getRewardExp() != null ? reward.getRewardExp() : 0));
            user.setGold(user.getGold() + (reward.getRewardGold() != null ? reward.getRewardGold() : 0));
            user.setWoods(user.getWoods() + (reward.getRewardWood() != null ? reward.getRewardWood() : 0));
            user.setStones(user.getStones() + (reward.getRewardStone() != null ? reward.getRewardStone() : 0));
        }

        // 4. Lưu User và cập nhật tiến độ
        userRepository.save(user);

        saveCompletionProgress(user, quest);

        return ApiResponseDto.<User>builder()
                .status(200)
                .message("Mở hòm quà thành công!")
                .data(user)
                .build();
    }

    private void saveCompletionProgress(User user, Quest quest) {
        QuestProgress progress = progressRepository.findByUserIdAndQuestId(user.getId(), quest.getId())
                .orElse(new QuestProgress());
        progress.setUser(user);
        progress.setQuest(quest);
        progress.setIsCompleted(true);

        if (quest.getRewards() != null) {
            for (QuestReward reward : quest.getRewards()) {
                progress.setEarnedExp(reward.getRewardExp() != null ? reward.getRewardExp() : 0);
                progress.setEarnedGold(reward.getRewardGold() != null ? reward.getRewardGold() : 0);
                progress.setEarnedStone(reward.getRewardStone() != null ? reward.getRewardStone() : 0);
                progress.setEarnedWood(reward.getRewardWood() != null ? reward.getRewardWood() : 0);
            }
        }
        progressRepository.save(progress);
    }

    @Transactional
    public ApiResponseDto<QuestReward> createReward(QuestRewardRequestDto req) {
        // 1. Kiểm tra Quest có tồn tại không
        Quest quest = questRepository.findById(req.getQuestId())
                .orElseThrow(() -> new RuntimeException("Ải không tồn tại"));

        // 2. Tạo đối tượng QuestReward mới
        QuestReward reward = QuestReward.builder()
                .quest(quest)
                .rewardExp(req.getRewardExp() != null ? req.getRewardExp() : 0)
                .rewardGold(req.getRewardGold() != null ? req.getRewardGold() : 0)
                .rewardStone(req.getRewardStone() != null ? req.getRewardStone() : 0)
                .rewardWood(req.getRewardWood() != null ? req.getRewardWood() : 0)
                .build();

        try {
            QuestReward savedReward = rewardRepository.save(reward);
            return ApiResponseDto.<QuestReward>builder()
                    .status(201)
                    .message("Thiết lập phần thưởng thành công!")
                    .data(savedReward)
                    .build();
        } catch (Exception e) {
            return ApiResponseDto.<QuestReward>builder()
                    .status(500)
                    .message("Lỗi khi lưu phần thưởng: " + e.getMessage())
                    .build();
        }
    }
}
