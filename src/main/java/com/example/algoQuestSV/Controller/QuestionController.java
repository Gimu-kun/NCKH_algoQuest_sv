package com.example.algoQuestSV.Controller;

import com.example.algoQuestSV.Dto.Api.ApiResponseDto;
import com.example.algoQuestSV.Dto.Question.QuestionCreationDto;
import com.example.algoQuestSV.Dto.Quiz.QuizResultResponse;
import com.example.algoQuestSV.Dto.Quiz.QuizSubmissionRequest;
import com.example.algoQuestSV.Entity.Question;
import com.example.algoQuestSV.Service.QuestionService;
import com.example.algoQuestSV.Service.QuizService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @Autowired
    QuizService quizService;

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

    @PostMapping("/submit-quiz")
    public ResponseEntity<ApiResponseDto<QuizResultResponse>> submitQuiz(
            @RequestBody QuizSubmissionRequest request
    ) {
        // Gọi service xử lý logic chấm điểm và lưu progress
        QuizResultResponse result = quizService.submitQuiz(request);

        // Trả về kết quả bọc trong ApiResponseDto chuẩn của bạn
        ApiResponseDto<QuizResultResponse> response = new ApiResponseDto<>();
        response.setData(result);
        response.setMessage("Nộp bài thành công!");
        response.setStatus(HttpStatus.OK.value());

        return ResponseEntity.ok(response);
    }
}
