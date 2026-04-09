package com.example.algoQuestSV.Controller;

import com.example.algoQuestSV.Dto.Api.ApiResponseDto;
import com.example.algoQuestSV.Dto.Challenge.CurrentChallengeStateDto;
import com.example.algoQuestSV.Dto.Challenge.StageDetailType;
import com.example.algoQuestSV.Dto.Challenge.StageGeneralType;
import com.example.algoQuestSV.Service.ChallengeService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
