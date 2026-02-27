package com.example.algoQuestSV.Controller;

import com.example.algoQuestSV.Dto.Api.ApiResponseDto;
import com.example.algoQuestSV.Entity.QuestProgress;
import com.example.algoQuestSV.Service.QuestProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quest-progress")
@RequiredArgsConstructor
public class QuestProgressController {

    private final QuestProgressService progressService;

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
}
