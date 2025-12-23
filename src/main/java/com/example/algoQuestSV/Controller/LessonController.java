package com.example.algoQuestSV.Controller;

import com.example.algoQuestSV.Dto.Api.ApiResponseDto;
import com.example.algoQuestSV.Dto.Lesson.LessonCreationDto;
import com.example.algoQuestSV.Entity.Lesson;
import com.example.algoQuestSV.Service.LessonService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/lessons")
@CrossOrigin("*")
public class LessonController {
    @Autowired
    LessonService lessonService;

    @GetMapping
    public ApiResponseDto<List<Lesson>> getAll(){
        return lessonService.getAll();
    }

    @GetMapping("/{id}")
    public ApiResponseDto<Lesson> getById(@PathVariable String id){
        return lessonService.getById(id);
    }

    @PostMapping
    public ApiResponseDto<Lesson> create(@Valid @ModelAttribute LessonCreationDto req){
        System.out.println(req.toString());
        return lessonService.create(req);
    }
}
