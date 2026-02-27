package com.example.algoQuestSV.Controller;

import com.example.algoQuestSV.Dto.Api.ApiResponseDto;
import com.example.algoQuestSV.Dto.Quest.QuestPrerequisiteRequestDto;
import com.example.algoQuestSV.Entity.QuestPrerequisite;
import com.example.algoQuestSV.Service.QuestPrerequisiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quest-prerequisites")
@RequiredArgsConstructor
public class QuestPrerequisiteController {

    private final QuestPrerequisiteService prerequisiteService;

    @GetMapping("/quest/{questId}")
    public ResponseEntity<ApiResponseDto<List<QuestPrerequisite>>> getByQuest(@PathVariable String questId) {
        return ResponseEntity.ok(ApiResponseDto.<List<QuestPrerequisite>>builder()
                .status(200)
                .message("Lấy danh sách điều kiện thành công")
                .data(prerequisiteService.getByQuestId(questId))
                .build());
    }

    @PostMapping
    public ResponseEntity<ApiResponseDto<QuestPrerequisite>> create(@RequestBody QuestPrerequisiteRequestDto request) {
        return ResponseEntity.ok(ApiResponseDto.<QuestPrerequisite>builder()
                .status(201)
                .message("Thêm điều kiện thành công")
                .data(prerequisiteService.create(request))
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<QuestPrerequisite>> update(
            @PathVariable String id,
            @RequestBody QuestPrerequisiteRequestDto request) {
        return ResponseEntity.ok(ApiResponseDto.<QuestPrerequisite>builder()
                .status(200)
                .message("Cập nhật điều kiện thành công")
                .data(prerequisiteService.update(id, request))
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> delete(@PathVariable String id) {
        prerequisiteService.delete(id);
        return ResponseEntity.ok(ApiResponseDto.<Void>builder()
                .status(200)
                .message("Xóa điều kiện thành công")
                .build());
    }
}
