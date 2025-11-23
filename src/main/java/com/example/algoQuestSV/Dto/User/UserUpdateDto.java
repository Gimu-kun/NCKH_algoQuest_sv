package com.example.algoQuestSV.Dto.User;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserUpdateDto {
    @NotBlank(message = "Mật khẩu xác nhận không được để trống")
    private String confirmPasswords;

    private String passwords;

    private String firstName;

    private String lastName;

    private Boolean role;
}
