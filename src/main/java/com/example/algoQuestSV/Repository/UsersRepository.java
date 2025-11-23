package com.example.algoQuestSV.Repository;

import com.example.algoQuestSV.Entity.User;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsersRepository extends JpaRepository<User,String> {
    boolean existsByUsername(String username);

    Optional<User> findByUsername(@Length(min = 4, message = "Tên tài khoản tối thiểu phải có 4 chữ cái") @NotNull(message = "Tên tài khoản không được để trống!") String username);
}
