package com.example.algoQuestSV.Service;

import com.example.algoQuestSV.Entity.Quest;
import com.example.algoQuestSV.Entity.QuestProgress;
import com.example.algoQuestSV.Entity.User;
import com.example.algoQuestSV.Repository.QuestProgressRepository;
import com.example.algoQuestSV.Repository.QuestsRepository;
import com.example.algoQuestSV.Repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestProgressService {

    private final QuestProgressRepository progressRepository;
    private final UsersRepository userRepository;
    private final QuestsRepository questRepository;

    public List<QuestProgress> getAllProgressByUser(String userId) {
        return progressRepository.findByUserId(userId);
    }

    @Transactional
    public QuestProgress updateOrCreateProgress(String userId, String questId, boolean completed, int exp, int wood, int stone) {
        QuestProgress progress = progressRepository.findByUserIdAndQuestId(userId, questId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
                    Quest quest = questRepository.findById(questId).orElseThrow(() -> new RuntimeException("Quest not found"));
                    return QuestProgress.builder()
                            .user(user)
                            .quest(quest)
                            .isCompleted(false)
                            .earnedExp(0)
                            .earnedWood(0)
                            .earnedStone(0)
                            .build();
                });

        // Nếu nhiệm vụ vừa được hoàn thành lần đầu
        if (!progress.getIsCompleted() && completed) {
            User user = progress.getUser();
            user.setExp(user.getExp() + exp);
            user.setWoods(user.getWoods() + wood);
            user.setStones(user.getStones() + stone);

            // Logic lên cấp đơn giản (Ví dụ: mỗi 1000 exp lên 1 cấp)
            user.setLevel(1 + (user.getExp() / 1000));

            userRepository.save(user);
        }

        progress.setIsCompleted(completed);
        progress.setEarnedExp(progress.getEarnedExp() + exp);
        progress.setEarnedWood(progress.getEarnedWood() + wood);
        progress.setEarnedStone(progress.getEarnedStone() + stone);

        return progressRepository.save(progress);
    }

    @Transactional
    public void deleteProgress(String id) {
        progressRepository.deleteById(id);
    }
}
