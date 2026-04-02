package com.example.algoQuestSV.Service;

import com.example.algoQuestSV.Dto.Api.ApiResponseDto;
import com.example.algoQuestSV.Dto.Quest.BaseQuestContentDto;
import com.example.algoQuestSV.Dto.Quest.QuestContentAdjustDto;
import com.example.algoQuestSV.Dto.Quest.QuestCreationDto;
import com.example.algoQuestSV.Dto.Quest.QuestUpdateDto;
import com.example.algoQuestSV.Entity.*;
import com.example.algoQuestSV.Enum.BloomType;
import com.example.algoQuestSV.Repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuestService {
    @Autowired
    QuestsRepository questsRepository;

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    TopicsRepository topicsRepository;

    @Autowired
    LessonRepository lessonRepository;

    @Autowired
    LessonService lessonService;

    @Autowired
    QuestionsRepository questionsRepository;

    @Autowired
    QuestProgressRepository questProgressRepo;

    @Autowired
    QuestAnswerStorageRepository questAnswerStorageRepo;

    @Autowired
    ObjectMapper objectMapper;

    //Lấy chỉ mục mới
    private Integer getNewIndex(String topicId) {
        return questsRepository
                .findTopByTopicId_IdOrderByIndexOrderDesc(topicId)
                .map(Quest::getIndexOrder)
                .orElse(0) + 1;
    }

    //Lấy tất cả ải
    public ApiResponseDto<List<Quest>> getAll(){
        List<Quest> quests = questsRepository.findAll(
                Sort.by(
                        Sort.Order.asc("topicId.id"),
                        Sort.Order.asc("indexOrder")
                )
        );
        return ApiResponseDto.<List<Quest>>builder()
                .status(200)
                .message("Lấy dữ liệu chương thành công")
                .data(quests)
                .build();
    };

    //Lấy thông tin ải bằng id
    public ApiResponseDto<Quest> getById(String id) {
        Optional<Quest> optQuest = questsRepository.findById(id);
        if(optQuest.isEmpty()){
            return ApiResponseDto.<Quest>builder()
                    .status(404)
                    .message("Không tìm thấy chương tương ứng")
                    .data(null)
                    .build();
        }
        return ApiResponseDto.<Quest>builder()
                .status(200)
                .message("Lấy thông tin chương thành công")
                .data(optQuest.get())
                .build();
    }

    //Lấy thông tin ải bằng id cho người chơi
    public ApiResponseDto<Quest> getByIdForStage(String stageId, String userId) {
        Quest quest = questsRepository.findById(stageId).orElseThrow(()->new RuntimeException("Không tìm thấy chương tương ứng"));
        //Kiểm tra phải lần đầu chơi hay không
        boolean isFirst = questProgressRepo.existsByUserIdAndQuestId(userId,stageId);

        if(!isFirst){
            List<QuestQuestion> filteredQuestQuestions = quest.getQuestions().stream()
                    .filter(qq -> qq.getDefaultMark() != null && qq.getDefaultMark())
                    .toList();
            quest.setQuestions(filteredQuestQuestions);
            return ApiResponseDto.<Quest>builder()
                    .status(200)
                    .message("Lấy thông tin chương thành công")
                    .data(quest)
                    .build();
        }else{
            List<QuestQuestion> questionList = quest.getQuestions();
            return ApiResponseDto.<Quest>builder()
                    .status(200)
                    .message("Lấy thông tin chương thành công")
                    .data(adaptiveDistribution(questionList,userId,stageId))
                    .build();
        }
    }

    private Quest adaptiveDistribution(List<QuestQuestion> questionList, String userId, String stageId) {
        System.out.println("\n========== DEBUG ADAPTIVE DISTRIBUTION (BLOOM + SUCCESS RATE) ==========");
        System.out.println("[Step 0] User: " + userId + " | Stage ID: " + stageId);

        // 1. Lấy phiên chơi gần nhất
        QuestProgress lastProgress = questProgressRepo.findTopByUserIdAndQuestIdOrderByCreatedAtDesc(userId, stageId);
        if (lastProgress == null) {
            System.out.println("[Step 1] Không tìm thấy phiên chơi cũ. Trả về câu hỏi mặc định.");
            return getQuestWithDefaultQuestions(stageId);
        }

        // 2. Lấy dữ liệu chi tiết từ Storage
        Optional<QuestAnswerStorage> storageOpt = questAnswerStorageRepo.findByQuestProgressId(lastProgress.getId());
        if (storageOpt.isEmpty()) {
            System.out.println("[Step 2] Không tìm thấy chi tiết câu trả lời cũ.");
            return getQuestWithDefaultQuestions(stageId);
        }

        try {
            List<Map<String, Object>> lastResults = objectMapper.readValue(
                    storageOpt.get().getResultDetail(),
                    new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {}
            );

            List<String> lastQuestionIds = lastResults.stream()
                    .map(res -> res.get("questionId").toString())
                    .toList();

            // Kiểm tra logic phím "isCorrect" hoặc "correct" tùy database của bạn
            List<String> failedIds = lastResults.stream()
                    .filter(res -> {
                        Object correct = res.get("isCorrect");
                        if (correct == null) correct = res.get("correct");
                        return correct != null && !(Boolean) correct;
                    })
                    .map(res -> res.get("questionId").toString())
                    .toList();

            double correctRate = (double) (lastQuestionIds.size() - failedIds.size()) / lastQuestionIds.size();
            System.out.println("[Step 2] Questions: " + lastQuestionIds.size() + " | Failed: " + failedIds.size());
            System.out.println("[Step 2.5] Tỉ lệ đúng phiên trước: " + (correctRate * 100) + "%");

            // 3. Chuẩn bị Pool câu hỏi mới (chưa làm ở phiên trước)
            List<QuestQuestion> availablePool = new ArrayList<>(questionList.stream()
                    .filter(qq -> !lastQuestionIds.contains(qq.getQuestion().getId()))
                    .toList());
            System.out.println("[Step 3] Available Pool Size: " + availablePool.size());

            List<QuestQuestion> newSelection = new ArrayList<>();
            int processedChanges = 0;

            // --- TRƯỜNG HỢP 1: TĂNG ĐỘ KHÓ (ĐÚNG >= 80%) ---
            if (correctRate >= 0.8) {
                System.out.println("[Mode] >>> TĂNG ĐỘ KHÓ (User xuất sắc) <<<");

                List<String> successIds = lastResults.stream()
                        .filter(res -> {
                            Object correct = res.get("isCorrect");
                            if (correct == null) correct = res.get("correct");
                            return correct != null && (Boolean) correct;
                        })
                        .map(res -> res.get("questionId").toString())
                        .collect(Collectors.toList());

                // Sắp xếp câu đúng từ DỄ đến KHÓ (Tỉ lệ đúng giảm dần)
                successIds.sort((id1, id2) -> Double.compare(
                        calculateSuccessRate(findInList(questionList, id2).getQuestion()),
                        calculateSuccessRate(findInList(questionList, id1).getQuestion())
                ));

                List<String> targetToUpgrade = successIds.stream().limit(2).toList();
                System.out.println("[Step 4] 2 câu dễ nhất sẽ nâng cấp: " + targetToUpgrade);

                for (String qId : lastQuestionIds) {
                    QuestQuestion currentQq = findInList(questionList, qId);
                    if (currentQq == null) continue;

                    if (targetToUpgrade.contains(qId) && processedChanges < 2 && !availablePool.isEmpty()) {
                        double currentRate = calculateSuccessRate(currentQq.getQuestion());
                        BloomType currentBloom = currentQq.getQuestion().getBloom();

                        // Tìm câu: Bloom >= hiện tại, Rate < hiện tại (Khó hơn)
                        Optional<QuestQuestion> hardMatch = availablePool.stream()
                                .filter(qq -> getBloomRank(qq.getQuestion().getBloom()) >= getBloomRank(currentBloom))
                                .filter(qq -> calculateSuccessRate(qq.getQuestion()) < currentRate)
                                .max(Comparator.comparingDouble(qq -> calculateSuccessRate(qq.getQuestion())));

                        if (hardMatch.isPresent()) {
                            QuestQuestion replacement = hardMatch.get();
                            newSelection.add(replacement);
                            availablePool.remove(replacement);
                            processedChanges++;
                            System.out.println("   [UPGRADE] " + qId + " (" + currentBloom + ", " + currentRate + ") -> "
                                    + replacement.getQuestion().getId() + " (" + replacement.getQuestion().getBloom() + ", " + calculateSuccessRate(replacement.getQuestion()) + ")");
                            continue;
                        } else {
                            System.out.println("   [KEEP] Không tìm thấy câu khó hơn phù hợp cho " + qId);
                        }
                    }
                    newSelection.add(currentQq);
                }
            }
            // --- TRƯỜNG HỢP 2: GIẢM ĐỘ KHÓ / THÍCH NGHI (KHI CÓ CÂU SAI) ---
            else {
                System.out.println("[Mode] >>> THÍCH NGHI GIẢM ĐỘ KHÓ (Hỗ trợ câu sai) <<<");
                for (String qId : lastQuestionIds) {
                    QuestQuestion currentQq = findInList(questionList, qId);
                    if (currentQq == null) continue;

                    if (failedIds.contains(qId) && processedChanges < 2 && !availablePool.isEmpty()) {
                        double failedRate = calculateSuccessRate(currentQq.getQuestion());
                        BloomType failedBloom = currentQq.getQuestion().getBloom();
                        QuestQuestion replacement = null;

                        // Hạ bậc Bloom từ hiện tại về R
                        for (int rank = getBloomRank(failedBloom); rank >= 1; rank--) {
                            BloomType targetBloom = getBloomByRank(rank);
                            Optional<QuestQuestion> match = availablePool.stream()
                                    .filter(qq -> qq.getQuestion().getBloom() == targetBloom)
                                    .filter(qq -> calculateSuccessRate(qq.getQuestion()) >= failedRate)
                                    .min((qq1, qq2) -> {
                                        double d1 = calculateSuccessRate(qq1.getQuestion()) - failedRate;
                                        double d2 = calculateSuccessRate(qq2.getQuestion()) - failedRate;
                                        if (Double.compare(d1, d2) != 0) return Double.compare(d1, d2);
                                        return Double.compare(calculateSuccessRate(qq2.getQuestion()), calculateSuccessRate(qq1.getQuestion()));
                                    });

                            if (match.isPresent()) {
                                replacement = match.get();
                                break;
                            }
                        }

                        if (replacement != null) {
                            newSelection.add(replacement);
                            availablePool.remove(replacement);
                            processedChanges++;
                            System.out.println("   [DOWNGRADE] " + qId + " (" + failedBloom + ", " + failedRate + ") -> "
                                    + replacement.getQuestion().getId() + " (" + replacement.getQuestion().getBloom() + ", " + calculateSuccessRate(replacement.getQuestion()) + ")");
                        } else {
                            System.out.println("   [KEEP] Không có câu thay thế dễ hơn cho " + qId);
                            newSelection.add(currentQq);
                        }
                    } else {
                        newSelection.add(currentQq);
                    }
                }
            }

            if (newSelection.size() < lastQuestionIds.size()) {
                System.out.println("[Warning] Thiếu câu hỏi! Đang bù đắp từ danh sách gốc...");
                for (String qId : lastQuestionIds) {
                    if (newSelection.stream().noneMatch(qq -> qq.getQuestion().getId().equals(qId))
                            && newSelection.size() < lastQuestionIds.size()) {
                        newSelection.add(findInList(questionList, qId));
                    }
                }
            }

            newSelection.sort(Comparator.comparingInt(qq -> getBloomRank(qq.getQuestion().getBloom())));

            shuffleMcqOptions(newSelection);

            System.out.println("[Final] New Session IDs: " + newSelection.stream().map(qq -> qq.getQuestion().getId()).toList());
            System.out.println("[Final] Total count: " + newSelection.size() + " (Match: " + (newSelection.size() == lastQuestionIds.size()) + ")");

            Quest quest = questsRepository.findById(stageId).orElseThrow();
            quest.setQuestions(newSelection);
            System.out.println("========== DEBUG END ==========\n");
            return quest;

        } catch (Exception e) {
            System.err.println("[Error] Adaptive logic error: " + e.getMessage());
            e.printStackTrace();
            return getQuestWithDefaultQuestions(stageId);
        }
    }

    // Thêm hàm này vào QuestService để xử lý xáo trộn options cho từng câu hỏi
    private void shuffleMcqOptions(List<QuestQuestion> questQuestions) {
        System.out.println("[Step 5] Đang xáo trộn các lựa chọn MCQ...");
        for (QuestQuestion qq : questQuestions) {
            Question q = qq.getQuestion();

            // Kiểm tra nếu là câu hỏi trắc nghiệm (MCQ) và có danh sách đáp án
            if (q.getMcqAnswers() != null && !q.getMcqAnswers().isEmpty()) {
                // Tạo bản sao để tránh làm thay đổi trạng thái của Entity trong Persistence Context
                List<AnswersMcq> shuffledOptions = new ArrayList<>(q.getMcqAnswers());
                Collections.shuffle(shuffledOptions);

                // Gán lại danh sách đã xáo trộn cho object Question hiện tại
                q.setMcqAnswers(shuffledOptions);
                System.out.println("   -> Đã xáo trộn đáp án cho câu: " + q.getId());
            }
        }
    }

    private Quest getQuestWithDefaultQuestions(String stageId) {
        Quest quest = questsRepository.findById(stageId).orElseThrow();
        List<QuestQuestion> defaults = quest.getQuestions().stream()
                .filter(qq -> qq.getDefaultMark() != null && qq.getDefaultMark())
                .toList();
        quest.setQuestions(defaults);
        return quest;
    }

    private double calculateSuccessRate(Question q) {
        int correct = (q.getCorrectCount() != null) ? q.getCorrectCount() : 0;
        int incorrect = (q.getIncorrectCount() != null) ? q.getIncorrectCount() : 0;
        int total = correct + incorrect;
        return (total == 0) ? 0.0 : (double) correct / total;
    }

    private QuestQuestion findInList(List<QuestQuestion> list, String id) {
        return list.stream().filter(qq -> qq.getQuestion().getId().equals(id)).findFirst().orElse(null);
    }

    private int getBloomRank(BloomType bloom) {
        if (bloom == null) return 0;
        return switch (bloom) {
            case BloomType.an -> 4;
            case BloomType.ap -> 3;
            case BloomType.u  -> 2;
            case BloomType.r  -> 1;
            default -> 0;
        };
    }

    private BloomType getBloomByRank(int rank) {
        return switch (rank) {
            case 4 -> BloomType.an;
            case 3 -> BloomType.ap;
            case 2 -> BloomType.u;
            case 1 -> BloomType.r;
            default -> null;
        };
    }

    private List<BloomType> getHigherOrEqualBloom(BloomType currentBloom) {
        int currentRank = getBloomRank(currentBloom);
        List<BloomType> higherOrEqual = new ArrayList<>();
        // Thứ tự bạn quy định: R(1), U(2), AP(3), AN(4)
        for (int i = currentRank; i <= 4; i++) {
            higherOrEqual.add(getBloomByRank(i));
        }
        return higherOrEqual;
    }

    //Tạo ải mới (trống)
    public ApiResponseDto<Quest> create(QuestCreationDto req){
        User operator = usersRepository.findById(req.getOperatorId())
                .orElseThrow(()->new RuntimeException("ID người thao tác không hợp lệ!"));

        try{
            Quest quest = Quest.builder()
                .title(req.getTitle())
                    .type(req.getType())
                    .questNum(req.getQuestNum())
                .description(req.getDescription())
                .createdBy(operator)
                .updatedBy(operator)
                .build();
            questsRepository.save(quest);
            return ApiResponseDto.<Quest>builder()
                    .status(200)
                    .message("Không tìm thấy chương tương ứng")
                    .data(quest)
                    .build();
        }catch (Exception ex){
            return ApiResponseDto.<Quest>builder()
                    .status(500)
                    .message("Lỗi khi lưu thông tin chương")
                    .data(null)
                    .build();
        }
    }

    //Cập nhật ải
    public ApiResponseDto<Quest> update(QuestUpdateDto req, String id){
        Optional<Quest> optQuest = questsRepository.findById(id);
        if (optQuest.isEmpty()){
            return ApiResponseDto.<Quest>builder()
                    .status(400)
                    .message("Không tìm thấy ải với id " + id)
                    .data(null)
                    .build();
        }
        Quest quest = optQuest.get();

        Optional<User> optUser = usersRepository.findById(req.getOperatorId());

        if (optUser.isEmpty()){
            return ApiResponseDto.<Quest>builder()
                    .status(400)
                    .message("ID người thao tác không hợp lệ!")
                    .data(null)
                    .build();
        }
        quest.setCreatedBy(optUser.get());
        quest.setUpdatedBy(optUser.get());

        Optional<Topic> optTopic = topicsRepository.findById(req.getTopicId());

        if (optTopic.isEmpty()){
            return ApiResponseDto.<Quest>builder()
                    .status(400)
                    .message("ID chương không hợp lệ!")
                    .data(null)
                    .build();
        }
        quest.setTopicId(optTopic.get());

        if(req.getTitle() != null){
            if (questsRepository.existsByTitleAndIdNot(req.getTitle(),id)){
                return ApiResponseDto.<Quest>builder()
                        .status(400)
                        .message("Tiêu đề " + req.getTitle() + " đã tồn tại")
                        .data(null)
                        .build();
            }
            quest.setTitle(req.getTitle());
        }

        if(req.getQuestNum() != null){
            quest.setQuestNum(req.getQuestNum());
        }

        if(req.getStatus() != null){
            quest.setStatus(req.getStatus());
        }

        if (req.getDescription() != null){
            quest.setDescription(req.getDescription());
        }

        try{
            questsRepository.save(quest);
            return ApiResponseDto.<Quest>builder()
                    .status(200)
                    .message("Cập nhật thông tin ải thành công!")
                    .data(quest)
                    .build();
        } catch (Exception e) {
            return ApiResponseDto.<Quest>builder()
                    .status(500)
                    .message("Lỗi khi cập nhật thông tin ải!" + e)
                    .data(null)
                    .build();
        }
    }

    public ApiResponseDto<Quest> changeOrderIndex(String id, Integer order){
        Optional<Quest> optQuest = questsRepository.findById(id);
        if (optQuest.isEmpty()){
            return ApiResponseDto.<Quest>builder()
                    .status(400)
                    .message("Không tìm thấy ải với id " + id)
                    .data(null)
                    .build();
        }
        Quest quest = optQuest.get();
        quest.setIndexOrder(order);
        try{
            questsRepository.save(quest);
            return ApiResponseDto.<Quest>builder()
                    .status(200)
                    .message("Cập nhật thứ tự ải thành công!")
                    .data(quest)
                    .build();
        }catch(Exception e){
            return ApiResponseDto.<Quest>builder()
                    .status(500)
                    .message("Lỗi khi cập nhật thứ tự ải!" + e)
                    .data(null)
                    .build();
        }
    }

    @Transactional
    public ApiResponseDto<Quest> contentAdjust(String id, QuestContentAdjustDto req) {
        Optional<Quest> optQuest = questsRepository.findById(id);
        if (optQuest.isEmpty()) {
            return ApiResponseDto.<Quest>builder().status(400).message("Không tìm thấy").build();
        }

        Quest quest = optQuest.get();
        try {
            // 1. Cập nhật Lessons
            if (req.getLessons() != null) {
                quest.getLessons().clear();
                for (BaseQuestContentDto item : req.getLessons()) {
                    lessonRepository.findById(item.getId()).ifPresent(lesson -> {
                        QuestLesson ql = new QuestLesson();
                        ql.setQuest(quest);
                        ql.setLesson(lesson);
                        quest.getLessons().add(ql);
                    });
                }
            }

            // 2. Cập nhật Questions
            if (req.getQuestions() != null) {
                quest.getQuestions().clear(); // Xóa các bản ghi cũ
                for (BaseQuestContentDto item : req.getQuestions()) {
                    questionsRepository.findById(item.getId()).ifPresent(question -> {
                        QuestQuestion qq = new QuestQuestion();
                        qq.setQuest(quest);
                        qq.setDefaultMark(item.getDefaultMark());
                        qq.setQuestion(question);
                        qq.setPoint(item.getPoint());
                        qq.setExp(item.getExp());
                        quest.getQuestions().add(qq);
                    });
                }
            }

            questsRepository.save(quest);
            return ApiResponseDto.<Quest>builder().status(200).data(quest).build();

        } catch (Exception e) {
            return ApiResponseDto.<Quest>builder().status(500).message(e.getMessage()).build();
        }
    }

    @Transactional
    public ApiResponseDto<Quest> addQuestToTopic(String topicId, String questId) {
        Optional<Quest> optQuest = questsRepository.findById(questId);
        Optional<Topic> optTopic = topicsRepository.findById(topicId);

        if (optQuest.isEmpty() || optTopic.isEmpty()) {
            return ApiResponseDto.<Quest>builder().status(400).message("Dữ liệu không hợp lệ").build();
        }

        Quest quest = optQuest.get();
        Topic topic = optTopic.get();

        // Vì 1 Quest chỉ thuộc 1 Topic, ta set thẳng topic mới
        quest.setTopicId(topic);

        // Đặt Quest vào cuối danh sách của Topic đó
        quest.setIndexOrder(getNewIndex(topicId));

        try {
            questsRepository.save(quest);
            return ApiResponseDto.<Quest>builder()
                    .status(200)
                    .message("Đã chuyển ải '" + quest.getTitle() + "' vào chương '" + topic.getTitle() + "'")
                    .data(quest)
                    .build();
        } catch (Exception e) {
            return ApiResponseDto.<Quest>builder().status(500).message("Lỗi: " + e.getMessage()).build();
        }
    }

    @Transactional
    public ApiResponseDto<Quest> removeQuestFromTopic(String questId) {
        Optional<Quest> optQuest = questsRepository.findById(questId);

        if (optQuest.isEmpty()) {
            return ApiResponseDto.<Quest>builder()
                    .status(404)
                    .message("Không tìm thấy màn chơi!")
                    .build();
        }

        Quest quest = optQuest.get();
        quest.setTopicId(null);
        quest.setIndexOrder(0);

        try {
            questsRepository.save(quest);
            return ApiResponseDto.<Quest>builder()
                    .status(200)
                    .message("Đã loại màn chơi khỏi chương thành công")
                    .data(quest)
                    .build();
        } catch (Exception e) {
            return ApiResponseDto.<Quest>builder()
                    .status(500)
                    .message("Lỗi: " + e.getMessage())
                    .build();
        }
    }

    @Transactional
    public ApiResponseDto<String> reorderQuests(List<String> questIds) {
        try {
            for (int i = 0; i < questIds.size(); i++) {
                String id = questIds.get(i);
                questsRepository.updateIndexOrder(id, i + 1); // Cập nhật index = thứ tự trong mảng + 1
            }
            return ApiResponseDto.<String>builder().status(200).message("Sắp xếp thứ tự thành công").build();
        } catch (Exception e) {
            return ApiResponseDto.<String>builder().status(500).message("Lỗi khi sắp xếp: " + e.getMessage()).build();
        }
    }
}
