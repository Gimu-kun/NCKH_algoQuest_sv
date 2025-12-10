package com.example.algoQuestSV.Service;

import com.example.algoQuestSV.Dto.Api.ApiResponseDto;
import com.example.algoQuestSV.Dto.Quest.QuestCreationDto;
import com.example.algoQuestSV.Dto.Quest.QuestUpdateDto;
import com.example.algoQuestSV.Entity.Quest;
import com.example.algoQuestSV.Entity.Topic;
import com.example.algoQuestSV.Entity.User;
import com.example.algoQuestSV.Repository.QuestsRepository;
import com.example.algoQuestSV.Repository.TopicsRepository;
import com.example.algoQuestSV.Repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    //Lấy chỉ mục mới
    private Integer getNewIndex(String topicId) {
        return questsRepository
                .findTopByTopicId_IdOrderByIndexOrderDesc(topicId)
                .map(Quest::getIndexOrder)
                .orElse(0) + 1;
    }

    //Lấy tất cả ải
    public ApiResponseDto<List<Quest>> getAll(){
        List<Quest> quests = questsRepository.findAll();
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

    //Tạo ải mới
    public ApiResponseDto<Quest> create(QuestCreationDto req){
        Optional<User> optUser = usersRepository.findById(req.getOperatorId());

        if (optUser.isEmpty()){
            return ApiResponseDto.<Quest>builder()
                    .status(400)
                    .message("ID người thao tác không hợp lệ!")
                    .data(null)
                    .build();
        }

        User operator = optUser.get();

        try{
            Quest quest = Quest.builder()
                .questType(req.getQuestType())
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

    //Thêm ải vào chương
    public ApiResponseDto<Quest> addQuestToTopic(String topicId, String questId){
        Optional<Quest> optQuest = questsRepository.findById(questId);
        if (optQuest.isEmpty()){
            return ApiResponseDto.<Quest>builder()
                    .status(400)
                    .message("Không tìm thấy ải với id " + questId)
                    .data(null)
                    .build();
        }
        Quest quest = optQuest.get();

        if(topicId != null){
            Optional<Topic> optTopic = topicsRepository.findById(topicId);

            if (optTopic.isEmpty()){
                return ApiResponseDto.<Quest>builder()
                        .status(400)
                        .message("ID chương không hợp lệ!")
                        .data(null)
                        .build();
            }
            quest.setTopicId(optTopic.get());
        }
        quest.setIndexOrder(getNewIndex(topicId));
        try{
            questsRepository.save(quest);
            return ApiResponseDto.<Quest>builder()
                    .status(200)
                    .message("Thêm ải vào chương thành công!")
                    .data(quest)
                    .build();
        }catch(Exception e){
            return ApiResponseDto.<Quest>builder()
                    .status(500)
                    .message("Lỗi khi thêm ải vào chương!" + e)
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
}
