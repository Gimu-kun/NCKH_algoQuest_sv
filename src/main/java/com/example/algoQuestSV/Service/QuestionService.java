package com.example.algoQuestSV.Service;

import com.example.algoQuestSV.Dto.Answer.AnswerMcqCreationDto;
import com.example.algoQuestSV.Dto.Answer.AnswerMpCreationDto;
import com.example.algoQuestSV.Dto.Api.ApiResponseDto;
import com.example.algoQuestSV.Dto.Question.QuestionCreationDto;
import com.example.algoQuestSV.Entity.*;
import com.example.algoQuestSV.Enum.QuestionType;
import com.example.algoQuestSV.Repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Service
public class QuestionService {
    @Autowired
    QuestsRepository questsRepository;
    @Autowired
    QuestionImgsRepository questionImgsRepository;
    @Autowired
    TopicsRepository topicsRepository;
    @Autowired
    QuestionsRepository questionsRepository;
    @Autowired
    UsersRepository usersRepository;
    @Autowired
    AnswerService answerService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    private List<String> saveFiles(MultipartFile[] files) throws IOException {
        List<String> filePaths = new ArrayList<>();

        if (files == null || files.length == 0) {
            return filePaths;
        }

        File directory = new File(uploadDir);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Could not create upload directory");
        }

        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                String originalName = Paths.get(Objects.requireNonNull(file.getOriginalFilename())).getFileName().toString();
                String fileName = UUID.randomUUID() + "_" + originalName;

                Path copyLocation = Paths.get(uploadDir, fileName);
                Files.copy(file.getInputStream(), copyLocation, StandardCopyOption.REPLACE_EXISTING);

                filePaths.add("/uploads/" + fileName);
            }
        }
        return filePaths;
    }

    @Transactional
    public ApiResponseDto<Question> create(QuestionCreationDto req) throws IOException {
        //Xác thực thông tin người thao tác
        Optional<User> optUser = usersRepository.findById(req.getOperatorId());

        if (optUser.isEmpty()){
            return ApiResponseDto.<Question>builder()
                    .status(400)
                    .message("ID người thao tác không hợp lệ!")
                    .data(null)
                    .build();
        }

        if(!topicsRepository.existsById(req.getTopicId())){
            return ApiResponseDto.<Question>builder()
                    .status(400)
                    .message("ID chủ đề không tồn tại!")
                    .data(null)
                    .build();
        }

        User operator = optUser.get();
            //Tạo câu hỏi mới
            Question question = new Question();
            question.setTopicId(req.getTopicId());
            question.setQuestionType(req.getQuestionType());
            question.setStatus(req.getStatus());
            question.setQuestionContent(req.getQuestionContent());
            question.setIndexOrder(req.getIndexOrder());
            question.setBloom(req.getBloom());
            question.setCreatedBy(operator);
            question.setUpdatedBy(operator);
            questionsRepository.save(question);

            //Tạo đường dẫn hình ảnh
            List<String> urls = saveFiles(
                    req.getImgs() != null
                            ? req.getImgs().toArray(new MultipartFile[0])
                            : new MultipartFile[0]
            );

            List<QuestionImgs> questionImgsList = new ArrayList<>();
            for (int i = 0; i < urls.size(); i++) {
                QuestionImgs questionImgs = new QuestionImgs();
                questionImgs.setQuestionId(question.getId());
                questionImgs.setUrl(urls.get(i));
                questionImgs.setIndexOrder(i + 1);
                questionImgsList.add(questionImgs);
            }

            if (!questionImgsList.isEmpty()) {
                questionImgsRepository.saveAll(questionImgsList);
            }

            switch (question.getQuestionType()){
                case QuestionType.fn:
                    answerService.create(req.getAnswerFn(),question.getId());
                    break;
                case QuestionType.fns:
                    answerService.create(req.getAnswerFns(),question.getId());
                    break;
                case QuestionType.fs:
                    System.out.println(req.getAnswerFs().toString());
                    answerService.create(req.getAnswerFs(),question.getId());
                    break;
                case QuestionType.mcq:
                    AnswerMcqCreationDto[] answersMcqs = req.getAnswerMcq();
                    for (AnswerMcqCreationDto answersMcq : answersMcqs) {
                        answerService.create(answersMcq,question.getId());
                    }
                    break;
                case QuestionType.mp:
                    AnswerMpCreationDto[] answerMps = req.getAnswerMp();
                    for (AnswerMpCreationDto answerMp : answerMps) {
                        answerService.create(answerMp,question.getId());
                    }
                    break;
                default:
                    break;
            }
            return  ApiResponseDto.<Question>builder()
                    .status(200)
                    .message("Tạo câu hỏi thành công!")
                    .data(question)
                    .build();
    }

    public ApiResponseDto<List<Question>> getAll(){
        return ApiResponseDto.<List<Question>>builder()
                .status(200)
                .message("Lấy danh sách câu hỏi thành công!")
                .data(questionsRepository.findAll())
                .build();
    }

    public ApiResponseDto<Question> getById(String id) {
        Optional<Question> optionalQuestion = questionsRepository.findById(id);
        if (optionalQuestion.isPresent()){
            return ApiResponseDto.<Question>builder()
                    .status(200)
                    .message("Lấy thông tin câu hỏi thành công!")
                    .data(optionalQuestion.get())
                    .build();
        }
        return ApiResponseDto.<Question>builder()
                .status(404)
                .message("Không tìm thấy thông tin câu hỏi!")
                .data(null)
                .build();
    }

    @Transactional
    public ApiResponseDto<Question> update(String id, QuestionCreationDto req) throws IOException {
        System.out.println(req.toString());
        // 1. Kiểm tra câu hỏi có tồn tại không
        Optional<Question> optQuestion = questionsRepository.findById(id);
        if (optQuestion.isEmpty()) {
            return ApiResponseDto.<Question>builder()
                    .status(404)
                    .message("Không tìm thấy câu hỏi để cập nhật!")
                    .build();
        }

        // 2. Xác thực người thao tác và chủ đề
        Optional<User> optUser = usersRepository.findById(req.getOperatorId());
        if (optUser.isEmpty()) {
            return ApiResponseDto.<Question>builder().status(400).message("Người thao tác không hợp lệ!").build();
        }
        if (!topicsRepository.existsById(req.getTopicId())) {
            return ApiResponseDto.<Question>builder().status(400).message("Chủ đề không tồn tại!").build();
        }

        Question question = optQuestion.get();
        User operator = optUser.get();

        // 3. Cập nhật thông tin cơ bản
        question.setTopicId(req.getTopicId());
        question.setQuestionType(req.getQuestionType());
        question.setQuestionContent(req.getQuestionContent());
        question.setBloom(req.getBloom());
        question.setStatus(req.getStatus());
        question.setUpdatedBy(operator);
        questionsRepository.save(question);

        // 4. Xử lý hình ảnh mới (nếu có)
        if (req.getImgs() != null && !req.getImgs().isEmpty()) {
            // Lưu ý: Tùy nghiệp vụ bạn có thể xóa ảnh cũ trong thư mục uploads ở đây
            List<String> urls = saveFiles(req.getImgs().toArray(new MultipartFile[0]));
            List<QuestionImgs> newImgs = new ArrayList<>();
            for (int i = 0; i < urls.size(); i++) {
                QuestionImgs img = new QuestionImgs();
                img.setQuestionId(question.getId());
                img.setUrl(urls.get(i));
                img.setIndexOrder(i + 1);
                newImgs.add(img);
            }
            questionImgsRepository.saveAll(newImgs);
        }

        // 5. QUAN TRỌNG: Xóa đáp án cũ trước khi tạo mới
        // Bạn cần bổ sung phương thức deleteAnswersByQuestionId trong AnswerService
        answerService.deleteAnswersByQuestionId(question.getId());

        // 6. Tạo đáp án mới dựa trên loại câu hỏi
        switch (question.getQuestionType()) {
            case QuestionType.mcq:
                for (AnswerMcqCreationDto dto : req.getAnswerMcq()) {
                    answerService.create(dto, question.getId());
                }
                break;
            case QuestionType.fn:
                answerService.create(req.getAnswerFn(), question.getId());
                break;
            case QuestionType.fns:
                answerService.create(req.getAnswerFns(), question.getId());
                break;
            case QuestionType.fs:
                answerService.create(req.getAnswerFs(), question.getId());
                break;
            case QuestionType.mp:
                for (AnswerMpCreationDto dto : req.getAnswerMp()) {
                    answerService.create(dto, question.getId());
                }
                break;
        }

        return ApiResponseDto.<Question>builder()
                .status(200)
                .message("Cập nhật câu hỏi thành công!")
                .data(question)
                .build();
    }
}
