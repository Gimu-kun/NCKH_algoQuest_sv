package com.example.algoQuestSV.Service;

import com.example.algoQuestSV.Entity.Streak;
import com.example.algoQuestSV.Entity.User;
import com.example.algoQuestSV.Repository.StreaksRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class StreakService {
    @Autowired
    StreaksRepository streaksRepository;

    private void addNew(User user, Integer streakCount){
        Streak streak = Streak.builder()
                .user(user)
                .accessAt(LocalDateTime.now())
                .currentStreak(streakCount)
                .build();
        streaksRepository.save(streak);
    }

    public void checkUpdate(User user){
        Optional<Streak> lastStreakOptional = streaksRepository.findFirstByUserIdOrderByAccessAtDesc(user.getId());
        //Chưa có record nào thì tạo record streak mới với streakCount = 1
        if (lastStreakOptional.isEmpty()){
            addNew(user,1);
        }else {
            Streak lastStreak = lastStreakOptional.get();
            LocalDate lastStreakDate = lastStreak.getAccessAt().toLocalDate();
            LocalDate currentDate = LocalDate.now();
            LocalDate yesterday = currentDate.minusDays(1);
            //Nếu hqua có streak, tạo 1 record mới với streakCount + 1
            if (lastStreakDate.equals(yesterday)) {
                addNew(user, lastStreak.getCurrentStreak() + 1);
            }
            //Nếu hqua chưa có thì tạo streak mới với streakCount = 1
            else if (!lastStreakDate.equals(currentDate)){
                addNew(user, 1);
            }
        }
    }
}
