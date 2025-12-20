package com.example.algoQuestSV.Service;

import com.example.algoQuestSV.Dto.Api.ApiResponseDto;
import com.example.algoQuestSV.Dto.Topic.TopicCreationDto;
import com.example.algoQuestSV.Dto.Topic.TopicUpdateDto;
import com.example.algoQuestSV.Entity.Quest;
import com.example.algoQuestSV.Entity.Topic;
import com.example.algoQuestSV.Entity.User;
import com.example.algoQuestSV.Repository.TopicsRepository;
import com.example.algoQuestSV.Repository.UsersRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;

@Service
public class TopicService {
    @Autowired
    TopicsRepository topicsRepository;

    @Autowired
    UsersRepository usersRepository;

    //Lấy chỉ mục mới
    private Integer getNewIndex(){
        return topicsRepository.findMaxIndexOrder().orElse(0) + 1;
    };

    //Lấy tất cả chương
    public ApiResponseDto<List<Topic>> getAll(){
        List<Topic> topics = topicsRepository.findAll(Sort.by(Sort.Direction.ASC, "indexOrder"));
        return ApiResponseDto.<List<Topic>>builder()
                .status(200)
                .message("Lấy dữ liệu chương thành công")
                .data(topics)
                .build();
    }

    //Sắp xếp lại các thứ tự
    public void reindexTopics() {
        List<Topic> topics = topicsRepository
                .findAll(Sort.by(Sort.Direction.ASC, "indexOrder"));

        int index = 1;
        for (Topic topic : topics) {
            topic.setIndexOrder(index++);
        }

        topicsRepository.saveAll(topics);
    }

    //Tạo mới chương
    public ApiResponseDto<Topic> create(TopicCreationDto req){
        if (topicsRepository.existsByTitle(req.getTitle())){
            return ApiResponseDto.<Topic>builder()
                    .status(400)
                    .message("Tiêu đề đã tồn tại!")
                    .data(null)
                    .build();
        }

        Optional<User> optUser = usersRepository.findById(req.getOperatorId());

        if (optUser.isEmpty()){
            return ApiResponseDto.<Topic>builder()
                    .status(400)
                    .message("ID người thao tác không hợp lệ!")
                    .data(null)
                    .build();
        }

        User operator = optUser.get();

        try{
            Topic topic = Topic.builder()
                    .title(req.getTitle())
                    .description(req.getDescription())
                    .status(false)
                    .createdBy(operator)
                    .updatedBy(operator)
                    .indexOrder(getNewIndex())
                    .build();
            topicsRepository.save(topic);
            return ApiResponseDto.<Topic>builder()
                    .status(200)
                    .message("Thêm tiêu đề thành công")
                    .data(topic)
                    .build();
        }catch(Exception e){
            System.err.println("Lỗi khi lưu Topic: " + e.getMessage());

            return ApiResponseDto.<Topic>builder()
                    .status(500)
                    .message("Lỗi hệ thống khi thêm tiêu đề: " + e.getMessage())
                    .data(null)
                    .build();
        }
    };

    //Cập nhật thông tin chương
    @Transactional
    public ApiResponseDto<Topic> update(TopicUpdateDto req, String id){
        Optional<Topic> optionalTopic = topicsRepository.findById(id);
        if (optionalTopic.isEmpty()){
            return ApiResponseDto.<Topic>builder()
                    .status(404)
                    .message("ID chương không tồn tại!")
                    .data(null)
                    .build();
        }
        Topic topic = optionalTopic.get();

        Optional<User> optUser = usersRepository.findById(req.getOperatorId());

        if (optUser.isEmpty()){
            return ApiResponseDto.<Topic>builder()
                    .status(400)
                    .message("ID người thao tác không hợp lệ!")
                    .data(null)
                    .build();
        }
        topic.setUpdatedBy(optUser.get());

        if (req.getTitle() != null){
            if(topicsRepository.existsByTitleAndIdNot(req.getTitle(),id)){
                return ApiResponseDto.<Topic>builder()
                        .status(400)
                        .message("Tiêu đề đã tồn tại!")
                        .data(null)
                        .build();
            }
            topic.setTitle(req.getTitle());
        }

        if (req.getDescription() != null){
            topic.setDescription(req.getDescription());
        }

        if (req.getIndexOrder() != null){
            topic.setIndexOrder(req.getIndexOrder());
        }

        if (req.getStatus() != null){
            topic.setStatus(req.getStatus());
        }


        try{
            topicsRepository.save(topic);
            reindexTopics();

            return ApiResponseDto.<Topic>builder()
                    .status(200)
                    .message("Cập nhật thông tin chương thành công!")
                    .data(topic)
                    .build();
        }catch (Exception e){
            System.err.println("Lỗi khi cập nhật Topic: " + e.getMessage());

            return ApiResponseDto.<Topic>builder()
                    .status(500)
                    .message("Lỗi hệ thống khi cập nhật tiêu đề: " + e.getMessage())
                    .data(null)
                    .build();
        }
    };

    //Lấy thông tin chương bằng id
    public ApiResponseDto<Topic> getTopicById(String id) {
        Optional<Topic> optTopic = topicsRepository.findById(id);
        if(optTopic.isEmpty()){
            return ApiResponseDto.<Topic>builder()
                    .status(404)
                    .message("Không tìm thấy chương tương ứng")
                    .data(null)
                    .build();
        }
        return ApiResponseDto.<Topic>builder()
                .status(200)
                .message("Không tìm thấy chương tương ứng")
                .data(optTopic.get())
                .build();
    }
};
