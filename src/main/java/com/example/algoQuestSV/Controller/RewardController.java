package com.example.algoQuestSV.Controller;

import com.example.algoQuestSV.Dto.Api.ApiResponseDto;
import com.example.algoQuestSV.Dto.Quest.QuestRewardRequestDto;
import com.example.algoQuestSV.Entity.QuestReward;
import com.example.algoQuestSV.Entity.User;
import com.example.algoQuestSV.Service.RewardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rewards")
@CrossOrigin("*")
public class RewardController {

    @Autowired
    private RewardService rewardService;

    @PostMapping("/claim-bonus")
    public ResponseEntity<ApiResponseDto<User>> claimBonus(
            @RequestParam String questId,
            @RequestParam String userId) {

        ApiResponseDto<User> result = rewardService.claimBonusReward(questId, userId);
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    @PostMapping("/setup")
    public ResponseEntity<ApiResponseDto<QuestReward>> setupReward(@RequestBody QuestRewardRequestDto req) {
        ApiResponseDto<QuestReward> result = rewardService.createReward(req);
        return ResponseEntity.status(result.getStatus()).body(result);
    }
}
