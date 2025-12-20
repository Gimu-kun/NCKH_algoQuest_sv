package com.example.algoQuestSV.Service;
import com.example.algoQuestSV.Dto.Answer.*;
import com.example.algoQuestSV.Dto.Api.ApiResponseDto;
import com.example.algoQuestSV.Entity.*;
import com.example.algoQuestSV.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnswerService {
    @Autowired
    AnswersFnRepository answersFnRepository;

    @Autowired
    AnswersFnsRepository answersFnsRepository;

    @Autowired
    AnswersMcqRepository answersMcqRepository;

    @Autowired
    AnswersFsRepository answersFsRepository;

    @Autowired
    AnswersMpRepository answersMpRepository;

    public void create(AnswerFnCreationDto req, String questionId){
        AnswersFn answersFn = new AnswersFn(req.getAnswer(), req.getTolerance());
        answersFn.setQuestionId(questionId);
        answersFnRepository.save(answersFn);
    }

    public void create(AnswerFnsCreationDto req, String questionId){
        AnswersFns answersFns = new AnswersFns(req.getAnswer());
        answersFns.setQuestionId(questionId);
        answersFnsRepository.save(answersFns);
    }

    public void create(AnswerFsCreationDto req, String questionId){
        AnswersFs answersFs = new AnswersFs(req.getAnswer(), req.getSynonyms());
        answersFs.setQuestionId(questionId);
        answersFsRepository.save(answersFs);
    }

    public void create(AnswerMcqCreationDto req, String questionId){
        AnswersMcq answersMcq = new AnswersMcq(req.getContent(),req.getIsCorrect());
        answersMcq.setQuestionId(questionId);
        answersMcqRepository.save(answersMcq);
    }

    public void create(AnswerMpCreationDto req, String questionId){
        AnswersMp answersMp = new AnswersMp(req.getColumn1(),req.getColumn2());
        answersMp.setQuestionId(questionId);
        answersMpRepository.save(answersMp);
    }
}
