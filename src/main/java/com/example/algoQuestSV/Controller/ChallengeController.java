package com.example.algoQuestSV.Controller;

import com.example.algoQuestSV.Dto.Api.ApiResponseDto;
import com.example.algoQuestSV.Dto.Challenge.CurrentChallengeStateDto;
import com.example.algoQuestSV.Service.ChallengeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/challenge")
@CrossOrigin("*")
public class ChallengeController {
    @Autowired
    ChallengeService challengeService;

    @GetMapping("/session-state/{userId}")
    public ApiResponseDto<CurrentChallengeStateDto> getSessionState(@PathVariable String userId){
        return challengeService.getSessionState(userId);
    }
}
