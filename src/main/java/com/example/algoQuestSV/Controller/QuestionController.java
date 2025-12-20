package com.example.algoQuestSV.Controller;

import com.example.algoQuestSV.Dto.Api.ApiResponseDto;
import com.example.algoQuestSV.Dto.Question.QuestionCreationDto;
import com.example.algoQuestSV.Entity.Question;
import com.example.algoQuestSV.Service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    public ApiResponseDto<Question> create(@Valid @ModelAttribute QuestionCreationDto req) throws IOException {
        return questionService.create(req);
    };
}
