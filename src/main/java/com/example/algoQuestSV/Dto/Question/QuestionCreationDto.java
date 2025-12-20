package com.example.algoQuestSV.Dto.Question;
import com.example.algoQuestSV.Dto.Answer.*;
import com.example.algoQuestSV.Entity.Quest;
import com.example.algoQuestSV.Enum.BloomType;
import com.example.algoQuestSV.Enum.QuestionType;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
public class QuestionCreationDto {
    private QuestionType questionType;
    private String topicId;
    private BloomType bloom;
    private Boolean status;
    private List<MultipartFile> imgs;
    private String questionContent;
    private Integer indexOrder;
    private String operatorId;
    private AnswerFnCreationDto answerFn;
    private AnswerFnsCreationDto answerFns;
    private AnswerFsCreationDto answerFs;
    private AnswerMcqCreationDto[] answerMcq;
    private AnswerMpCreationDto[] answerMp;
}
