package com.example.algoQuestSV.Controller;

import com.example.algoQuestSV.Dto.Api.ApiResponseDto;
import com.example.algoQuestSV.Dto.Topic.TopicCreationDto;
import com.example.algoQuestSV.Dto.Topic.TopicUpdateDto;
import com.example.algoQuestSV.Entity.Quest;
import com.example.algoQuestSV.Entity.Topic;
import com.example.algoQuestSV.Service.TopicService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/topics")
@CrossOrigin("*")
public class TopicController {
    @Autowired
    TopicService topicService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<Topic>> create(@Valid @RequestBody TopicCreationDto req){
        ApiResponseDto<Topic> result = topicService.create(req);
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<List<Topic>>> getList(){
        return ResponseEntity.ok(topicService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Topic>> getUserById(@PathVariable String id){
        ApiResponseDto<Topic> result = topicService.getTopicById(id);
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Topic>> update(@Valid @RequestBody TopicUpdateDto req, @PathVariable String id){
        ApiResponseDto<Topic> result = topicService.update(req,id);
        return ResponseEntity.status(result.getStatus()).body(result);
    }
}
