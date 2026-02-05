package com.example.algoQuestSV.Controller;

import com.example.algoQuestSV.Dto.Api.ApiResponseDto;
import com.example.algoQuestSV.Dto.Question.QuestionCreationDto;
import com.example.algoQuestSV.Entity.Question;
import com.example.algoQuestSV.Service.QuestionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("api/questions")
@CrossOrigin("*")
public class QuestionController {
    @Autowired
    QuestionService questionService;

    @GetMapping
    public ApiResponseDto<List<Question>> getAll(){
        return questionService.getAll();
    };

    @GetMapping("/{id}")
    public ApiResponseDto<Question> getAll(@PathVariable String id){
        return questionService.getById(id);
    };

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ApiResponseDto<Question> create(
            @RequestPart("question") String questionJson,
            @RequestPart(value = "imgs", required = false) List<MultipartFile> imgs
    ) throws IOException {
        System.out.println("vào tới đây rồi nè!");
        ObjectMapper objectMapper = new ObjectMapper();
        QuestionCreationDto dto = objectMapper.readValue(questionJson, QuestionCreationDto.class);
        dto.setImgs(imgs);
        return questionService.create(dto);
    };

    @PutMapping(value = "/{id}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ApiResponseDto<Question> update(
            @PathVariable String id,
            @RequestPart("question") String questionJson,
            @RequestPart(value = "imgs", required = false) List<MultipartFile> imgs
    ) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        QuestionCreationDto dto = objectMapper.readValue(questionJson, QuestionCreationDto.class);
        dto.setImgs(imgs);
        return questionService.update(id, dto);
    }

}
