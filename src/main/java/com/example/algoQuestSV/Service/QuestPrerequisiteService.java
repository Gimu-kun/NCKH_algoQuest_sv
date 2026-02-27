package com.example.algoQuestSV.Service;

import com.example.algoQuestSV.Dto.Quest.QuestPrerequisiteRequestDto;
import com.example.algoQuestSV.Entity.Quest;
import com.example.algoQuestSV.Entity.QuestPrerequisite;
import com.example.algoQuestSV.Repository.QuestPrerequisiteRepository;
import com.example.algoQuestSV.Repository.QuestsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestPrerequisiteService {

    private final QuestPrerequisiteRepository prerequisiteRepository;
    private final QuestsRepository questRepository;

    public List<QuestPrerequisite> getByQuestId(String questId) {
        return prerequisiteRepository.findByQuestId(questId);
    }

    @Transactional
    public QuestPrerequisite create(QuestPrerequisiteRequestDto request) {
        Quest quest = questRepository.findById(request.getQuestId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Quest mục tiêu"));

        QuestPrerequisite.QuestPrerequisiteBuilder builder = QuestPrerequisite.builder()
                .quest(quest)
                .requiredLevel(request.getRequiredLevel());

        if (request.getRequiredQuestId() != null) {
            Quest requiredQuest = questRepository.findById(request.getRequiredQuestId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Quest điều kiện"));
            builder.requiredQuest(requiredQuest);
        }

        return prerequisiteRepository.save(builder.build());
    }

    @Transactional
    public QuestPrerequisite update(String id, QuestPrerequisiteRequestDto request) {
        QuestPrerequisite pre = prerequisiteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy điều kiện này"));

        if (request.getRequiredQuestId() != null) {
            Quest reqQuest = questRepository.findById(request.getRequiredQuestId()).orElse(null);
            pre.setRequiredQuest(reqQuest);
        }
        pre.setRequiredLevel(request.getRequiredLevel());

        return prerequisiteRepository.save(pre);
    }

    @Transactional
    public void delete(String id) {
        prerequisiteRepository.deleteById(id);
    }
}