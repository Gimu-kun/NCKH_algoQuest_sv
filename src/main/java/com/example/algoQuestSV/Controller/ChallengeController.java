package com.example.algoQuestSV.Controller;

import com.example.algoQuestSV.Dto.Api.ApiResponseDto;
import com.example.algoQuestSV.Dto.Challenge.*;
import com.example.algoQuestSV.Dto.Visualization.VisualizationSubmitDto;
import com.example.algoQuestSV.Service.ChallengeService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/challenge")
@CrossOrigin("*")
public class ChallengeController {
    @Autowired
    ChallengeService challengeService;

    @GetMapping("/stages-list")
    public ApiResponseDto<List<StageGeneralType>> getStagesList(){
        return challengeService.getStageList();
    }

    @GetMapping("/session-state/{userId}")
    public ApiResponseDto<CurrentChallengeStateDto> getSessionState(@PathVariable String userId){
        return challengeService.getSessionState(userId);
    }

    @GetMapping("/escalation/{sessionId}")
    public ApiResponseDto<StageDetailType> challengeEscalation(@PathVariable Long sessionId) throws JsonProcessingException {
        return challengeService.challengeModeEscalation(sessionId);
    }

    @PostMapping("/submit")
    public ApiResponseDto<Map<String, Object>> submit(@RequestBody SubmitRequestDTO request) {
        return challengeService.submit(request);
    }

    @PostMapping("/submit-viz/{progressId}")
    public ApiResponseDto<Map<String, Object>> submitVisualizationResult(@PathVariable Long progressId,@RequestBody VisualizationSubmitDto request){
        return challengeService.submitVisualizationResult(progressId, request);
    }
}
