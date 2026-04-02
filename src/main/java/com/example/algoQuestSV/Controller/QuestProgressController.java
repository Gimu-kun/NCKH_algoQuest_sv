package com.example.algoQuestSV.Controller;

import com.example.algoQuestSV.Dto.Api.ApiResponseDto;
import com.example.algoQuestSV.Dto.Quiz.ReviewDetailResponse;
import com.example.algoQuestSV.Entity.QuestAnswerStorage;
import com.example.algoQuestSV.Entity.QuestProgress;
import com.example.algoQuestSV.Entity.Question;
import com.example.algoQuestSV.Repository.AnswersMcqRepository;
import com.example.algoQuestSV.Repository.QuestAnswerStorageRepository;
import com.example.algoQuestSV.Repository.QuestionsRepository;
import com.example.algoQuestSV.Service.QuestProgressService;
import com.example.algoQuestSV.Service.QuizService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quest-progress")
@RequiredArgsConstructor
@CrossOrigin("*")
public class QuestProgressController {

    private final QuestProgressService progressService;
    @Autowired
    QuizService quizService;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    QuestAnswerStorageRepository storageRepo;
    @Autowired
    QuestionsRepository questionsRepo;
    @Autowired
    AnswersMcqRepository mcqRepo;

    // Lấy tiến độ của một User
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponseDto<List<QuestProgress>>> getUserProgress(@PathVariable String userId) {
        return ResponseEntity.ok(ApiResponseDto.<List<QuestProgress>>builder()
                .status(200)
                .message("Lấy tiến độ thành công")
                .data(progressService.getAllProgressByUser(userId))
                .build());
    }

    // Cập nhật hoặc tạo mới tiến độ (Khi User làm xong Quest)
    @PostMapping("/update")
    public ResponseEntity<ApiResponseDto<QuestProgress>> updateProgress(
            @RequestParam String userId,
            @RequestParam String questId,
            @RequestParam boolean completed,
            @RequestParam int exp,
            @RequestParam int wood,
            @RequestParam int stone) {

        QuestProgress updated = progressService.updateOrCreateProgress(userId, questId, completed, exp, wood, stone);

        return ResponseEntity.ok(ApiResponseDto.<QuestProgress>builder()
                .status(200)
                .message("Cập nhật tiến độ và phần thưởng thành công")
                .data(updated)
                .build());
    }

    // Xóa bản ghi tiến độ (Dùng cho Admin hoặc Reset dữ liệu)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> deleteProgress(@PathVariable String id) {
        progressService.deleteProgress(id);
        return ResponseEntity.ok(ApiResponseDto.<Void>builder()
                .status(200)
                .message("Xóa bản ghi tiến độ thành công")
                .build());
    }

    @GetMapping("/{questId}/history")
    public ResponseEntity<ApiResponseDto<List<QuestProgress>>> getHistory(
            @PathVariable String questId,
            @RequestParam String userId
    ) {
        List<QuestProgress> history = progressService.getQuestHistory(userId, questId);

        ApiResponseDto<List<QuestProgress>> response = ApiResponseDto.<List<QuestProgress>>builder()
                .status(HttpStatus.OK.value())
                .message("Lấy lịch sử thành công")
                .data(history)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/review/{progressId}")
    public ResponseEntity<ApiResponseDto<ReviewDetailResponse>> getReview(@PathVariable String progressId) {
        ReviewDetailResponse data = quizService.getFullReviewDetail(progressId);
        return ResponseEntity.ok(ApiResponseDto.<ReviewDetailResponse>builder()
                .status(200)
                .message("Xóa bản ghi tiến độ thành công")
                .data(data)
                .build());
    }
}
