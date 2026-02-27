package com.example.algoQuestSV.Controller;

import com.example.algoQuestSV.Dto.Api.ApiResponseDto;
import com.example.algoQuestSV.Dto.Visualization.QuestVisualLinkRequestDto;
import com.example.algoQuestSV.Dto.Visualization.VisualBudgetSubmitRequestDto;
import com.example.algoQuestSV.Dto.Visualization.VisualSubmitRequestDto;
import com.example.algoQuestSV.Dto.Visualization.VisualizationRequestDto;
import com.example.algoQuestSV.Entity.QuestVisualization;
import com.example.algoQuestSV.Service.VisualizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/visualizations")
@CrossOrigin("*")
public class VisualizationController {

    @Autowired
    private VisualizationService visualService;

    // 1. API cho Admin: Tạo game mới
    @PostMapping("/admin/create")
    public ResponseEntity<?> create(@RequestBody VisualizationRequestDto dto) {
        return ResponseEntity.ok(visualService.createVisualization(dto));
    }

    // 2. API cho Admin: Gán game vào Quest (Ải)
    @PostMapping("/admin/link-quest")
    public ResponseEntity<?> linkToQuest(@RequestBody QuestVisualLinkRequestDto dto) {
        return ResponseEntity.ok(visualService.linkToQuest(dto));
    }

    // 3. API cho Client: Lấy dữ liệu trò chơi khi người chơi vào ải
    @GetMapping("/quest/{questId}")
    public ResponseEntity<ApiResponseDto<List<QuestVisualization>>> getByQuest(@PathVariable String questId) {
        List<QuestVisualization> data = visualService.getVisualsByQuest(questId);
        return ResponseEntity.ok(ApiResponseDto.<List<QuestVisualization>>builder()
                .status(200)
                .data(data)
                .build());
    }

    @PostMapping("/submit")
    public ResponseEntity<ApiResponseDto<?>> submitChallenge(@RequestBody VisualSubmitRequestDto dto) {
        ApiResponseDto<?> result = visualService.submitVisualChallenge(dto);
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    @PostMapping("/submit-budget-challenge")
    public ResponseEntity<ApiResponseDto<?>> submitBudgetChallenge(@RequestBody VisualBudgetSubmitRequestDto dto) {
        ApiResponseDto<?> result = visualService.submitBudgetChallenge(dto);
        return ResponseEntity.status(result.getStatus()).body(result);
    }
}
