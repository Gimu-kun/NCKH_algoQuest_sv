package com.example.algoQuestSV.Service;
import com.example.algoQuestSV.Dto.Answer.*;
import com.example.algoQuestSV.Dto.Api.ApiResponseDto;
import com.example.algoQuestSV.Entity.*;
import com.example.algoQuestSV.Enum.QuestionType;
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

    @Autowired
    QuestionsRepository questionsRepository;

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

    public void create(AnswerMpCreationDto req, String questionId) {

    }

    public void deleteAnswersByQuestionId(String id) {
        Question question = questionsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy câu hỏi"));

        question.getFsAnswers().clear();
        question.getFnAnswers().clear();
        question.getMpAnswers().clear();
        question.getMcqAnswers().clear();
        question.getFnsAnswers().clear();
    }
}
