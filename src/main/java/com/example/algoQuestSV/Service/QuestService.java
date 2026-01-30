package com.example.algoQuestSV.Service;

import com.example.algoQuestSV.Dto.Api.ApiResponseDto;
import com.example.algoQuestSV.Dto.Quest.BaseQuestContentDto;
import com.example.algoQuestSV.Dto.Quest.QuestContentAdjustDto;
import com.example.algoQuestSV.Dto.Quest.QuestCreationDto;
import com.example.algoQuestSV.Dto.Quest.QuestUpdateDto;
import com.example.algoQuestSV.Entity.*;
import com.example.algoQuestSV.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class QuestService {
    @Autowired
    QuestsRepository questsRepository;

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    TopicsRepository topicsRepository;

    @Autowired
    LessonRepository lessonRepository;

    @Autowired
    LessonService lessonService;

    @Autowired
    QuestionsRepository questionsRepository;

    //Lấy chỉ mục mới
    private Integer getNewIndex(String topicId) {
        return questsRepository
                .findTopByTopicId_IdOrderByIndexOrderDesc(topicId)
                .map(Quest::getIndexOrder)
                .orElse(0) + 1;
    }

    //Lấy tất cả ải
    public ApiResponseDto<List<Quest>> getAll(){
        List<Quest> quests = questsRepository.findAll(
                Sort.by(
                        Sort.Order.asc("topicId.id"),
                        Sort.Order.asc("indexOrder")
                )
        );
        return ApiResponseDto.<List<Quest>>builder()
                .status(200)
                .message("Lấy dữ liệu chương thành công")
                .data(quests)
                .build();
    };

    //Lấy thông tin ải bằng id
    public ApiResponseDto<Quest> getById(String id) {
        Optional<Quest> optQuest = questsRepository.findById(id);
        if(optQuest.isEmpty()){
            return ApiResponseDto.<Quest>builder()
                    .status(404)
                    .message("Không tìm thấy chương tương ứng")
                    .data(null)
                    .build();
        }
        return ApiResponseDto.<Quest>builder()
                .status(200)
                .message("Lấy thông tin chương thành công")
                .data(optQuest.get())
                .build();
    }

    //Tạo ải mới (trống)
    public ApiResponseDto<Quest> create(QuestCreationDto req){
        User operator = usersRepository.findById(req.getOperatorId())
                .orElseThrow(()->new RuntimeException("ID người thao tác không hợp lệ!"));

        try{
            Quest quest = Quest.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .createdBy(operator)
                .updatedBy(operator)
                .build();
            questsRepository.save(quest);
            return ApiResponseDto.<Quest>builder()
                    .status(200)
                    .message("Không tìm thấy chương tương ứng")
                    .data(quest)
                    .build();
        }catch (Exception ex){
            return ApiResponseDto.<Quest>builder()
                    .status(500)
                    .message("Lỗi khi lưu thông tin chương")
                    .data(null)
                    .build();
        }
    }

    //Cập nhật ải
    public ApiResponseDto<Quest> update(QuestUpdateDto req, String id){
        Optional<Quest> optQuest = questsRepository.findById(id);
        if (optQuest.isEmpty()){
            return ApiResponseDto.<Quest>builder()
                    .status(400)
                    .message("Không tìm thấy ải với id " + id)
                    .data(null)
                    .build();
        }
        Quest quest = optQuest.get();

        Optional<User> optUser = usersRepository.findById(req.getOperatorId());

        if (optUser.isEmpty()){
            return ApiResponseDto.<Quest>builder()
                    .status(400)
                    .message("ID người thao tác không hợp lệ!")
                    .data(null)
                    .build();
        }
        quest.setCreatedBy(optUser.get());
        quest.setUpdatedBy(optUser.get());

        Optional<Topic> optTopic = topicsRepository.findById(req.getTopicId());

        if (optTopic.isEmpty()){
            return ApiResponseDto.<Quest>builder()
                    .status(400)
                    .message("ID chương không hợp lệ!")
                    .data(null)
                    .build();
        }
        quest.setTopicId(optTopic.get());

        if(req.getTitle() != null){
            if (questsRepository.existsByTitleAndIdNot(req.getTitle(),id)){
                return ApiResponseDto.<Quest>builder()
                        .status(400)
                        .message("Tiêu đề " + req.getTitle() + " đã tồn tại")
                        .data(null)
                        .build();
            }
            quest.setTitle(req.getTitle());
        }

        if(req.getStatus() != null){
            quest.setStatus(req.getStatus());
        }

        if (req.getDescription() != null){
            quest.setDescription(req.getDescription());
        }

        try{
            questsRepository.save(quest);
            return ApiResponseDto.<Quest>builder()
                    .status(200)
                    .message("Cập nhật thông tin ải thành công!")
                    .data(quest)
                    .build();
        } catch (Exception e) {
            return ApiResponseDto.<Quest>builder()
                    .status(500)
                    .message("Lỗi khi cập nhật thông tin ải!" + e)
                    .data(null)
                    .build();
        }
    }

    public ApiResponseDto<Quest> changeOrderIndex(String id, Integer order){
        Optional<Quest> optQuest = questsRepository.findById(id);
        if (optQuest.isEmpty()){
            return ApiResponseDto.<Quest>builder()
                    .status(400)
                    .message("Không tìm thấy ải với id " + id)
                    .data(null)
                    .build();
        }
        Quest quest = optQuest.get();
        quest.setIndexOrder(order);
        try{
            questsRepository.save(quest);
            return ApiResponseDto.<Quest>builder()
                    .status(200)
                    .message("Cập nhật thứ tự ải thành công!")
                    .data(quest)
                    .build();
        }catch(Exception e){
            return ApiResponseDto.<Quest>builder()
                    .status(500)
                    .message("Lỗi khi cập nhật thứ tự ải!" + e)
                    .data(null)
                    .build();
        }
    }

    @Transactional
    public ApiResponseDto<Quest> contentAdjust(String id, QuestContentAdjustDto req) {
        Optional<Quest> optQuest = questsRepository.findById(id);
        if (optQuest.isEmpty()) {
            return ApiResponseDto.<Quest>builder().status(400).message("Không tìm thấy").build();
        }

        Quest quest = optQuest.get();
        try {
            // 1. Cập nhật Lessons
            if (req.getLessons() != null) {
                quest.getLessons().clear();
                for (BaseQuestContentDto item : req.getLessons()) {
                    lessonRepository.findById(item.getId()).ifPresent(lesson -> {
                        QuestLesson ql = new QuestLesson();
                        ql.setQuest(quest);
                        ql.setLesson(lesson);
                        ql.setPoint(item.getPoint());
                        ql.setExp(item.getExp());
                        quest.getLessons().add(ql);
                    });
                }
            }

            // 2. Cập nhật Questions
            if (req.getQuestions() != null) {
                quest.getQuestions().clear(); // Xóa các bản ghi cũ
                for (BaseQuestContentDto item : req.getQuestions()) {
                    questionsRepository.findById(item.getId()).ifPresent(question -> {
                        QuestQuestion qq = new QuestQuestion();
                        qq.setQuest(quest);
                        qq.setQuestion(question);
                        qq.setPoint(item.getPoint());
                        qq.setExp(item.getExp());
                        quest.getQuestions().add(qq);
                    });
                }
            }

            questsRepository.save(quest);
            return ApiResponseDto.<Quest>builder().status(200).data(quest).build();

        } catch (Exception e) {
            return ApiResponseDto.<Quest>builder().status(500).message(e.getMessage()).build();
        }
    }

    @Transactional
    public ApiResponseDto<Quest> addQuestToTopic(String topicId, String questId) {
        Optional<Quest> optQuest = questsRepository.findById(questId);
        Optional<Topic> optTopic = topicsRepository.findById(topicId);

        if (optQuest.isEmpty() || optTopic.isEmpty()) {
            return ApiResponseDto.<Quest>builder().status(400).message("Dữ liệu không hợp lệ").build();
        }

        Quest quest = optQuest.get();
        Topic topic = optTopic.get();

        // Vì 1 Quest chỉ thuộc 1 Topic, ta set thẳng topic mới
        quest.setTopicId(topic);

        // Đặt Quest vào cuối danh sách của Topic đó
        quest.setIndexOrder(getNewIndex(topicId));

        try {
            questsRepository.save(quest);
            return ApiResponseDto.<Quest>builder()
                    .status(200)
                    .message("Đã chuyển ải '" + quest.getTitle() + "' vào chương '" + topic.getTitle() + "'")
                    .data(quest)
                    .build();
        } catch (Exception e) {
            return ApiResponseDto.<Quest>builder().status(500).message("Lỗi: " + e.getMessage()).build();
        }
    }

    @Transactional
    public ApiResponseDto<Quest> removeQuestFromTopic(String questId) {
        Optional<Quest> optQuest = questsRepository.findById(questId);

        if (optQuest.isEmpty()) {
            return ApiResponseDto.<Quest>builder()
                    .status(404)
                    .message("Không tìm thấy màn chơi!")
                    .build();
        }

        Quest quest = optQuest.get();
        quest.setTopicId(null);
        quest.setIndexOrder(0);

        try {
            questsRepository.save(quest);
            return ApiResponseDto.<Quest>builder()
                    .status(200)
                    .message("Đã loại màn chơi khỏi chương thành công")
                    .data(quest)
                    .build();
        } catch (Exception e) {
            return ApiResponseDto.<Quest>builder()
                    .status(500)
                    .message("Lỗi: " + e.getMessage())
                    .build();
        }
    }

    @Transactional
    public ApiResponseDto<String> reorderQuests(List<String> questIds) {
        try {
            for (int i = 0; i < questIds.size(); i++) {
                String id = questIds.get(i);
                questsRepository.updateIndexOrder(id, i + 1); // Cập nhật index = thứ tự trong mảng + 1
            }
            return ApiResponseDto.<String>builder().status(200).message("Sắp xếp thứ tự thành công").build();
        } catch (Exception e) {
            return ApiResponseDto.<String>builder().status(500).message("Lỗi khi sắp xếp: " + e.getMessage()).build();
        }
    }
}
