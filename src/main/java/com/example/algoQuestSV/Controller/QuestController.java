package com.example.algoQuestSV.Controller;

import com.example.algoQuestSV.Dto.Api.ApiResponseDto;
import com.example.algoQuestSV.Dto.Lesson.LessonCreationDto;
import com.example.algoQuestSV.Dto.Quest.QuestContentAdjustDto;
import com.example.algoQuestSV.Dto.Quest.QuestCreationDto;
import com.example.algoQuestSV.Dto.Quest.QuestUpdateDto;
import com.example.algoQuestSV.Entity.Quest;
import com.example.algoQuestSV.Repository.QuestsRepository;
import com.example.algoQuestSV.Service.QuestService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/quests")
@CrossOrigin("*")
public class QuestController {
    @Autowired
    QuestService questService;

    @GetMapping
    public ApiResponseDto<List<Quest>> getAll(){
        return questService.getAll();
    }

    @GetMapping("/{id}")
    public ApiResponseDto<Quest> getById(@PathVariable String id){
        return questService.getById(id);
    }

    @PostMapping
    public ApiResponseDto<Quest> create(@Valid @RequestBody QuestCreationDto req){
        return questService.create(req);
    }

    @PatchMapping("/{id}")
    public ApiResponseDto<Quest> update(@Valid @RequestBody QuestUpdateDto req,@PathVariable String id){
        return questService.update(req,id);
    }

    @PatchMapping("/order-update")
    public ApiResponseDto<Quest> changeOrderIndex(@RequestParam Integer order, @RequestParam String quest){
        return questService.changeOrderIndex(quest,order);
    }

    @PatchMapping("cont-adj/{id}")
    public ApiResponseDto<Quest> contentAdjust(@RequestBody QuestContentAdjustDto req, @PathVariable String id){
        return questService.contentAdjust(id,req);
    }

    @PatchMapping("/add-to-topic")
    public ApiResponseDto<Quest> addToTopic(
            @RequestParam String topic,
            @RequestParam String quest) {
        return questService.addQuestToTopic(topic, quest);
    }

    @PatchMapping("/remove-from-topic/{id}")
    public ApiResponseDto<Quest> removeFromTopic(@PathVariable String id) {
        return questService.removeQuestFromTopic(id);
    }

    @PatchMapping("/reorder")
    public ApiResponseDto<String> reorder(@RequestBody List<String> questIds) {
        return questService.reorderQuests(questIds);
    }
}
