package com.example.algoQuestSV.Dto.User;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class UserCreationDto {
    @Length(min = 4, message = "Tên tài khoản tối thiểu phải có 4 chữ cái")
    @NotNull(message = "Tên tài khoản không được để trống!")
    private String username;

    @NotNull(message = "Mật khẩu không được để trống!")
    @Length(min = 4, message = "Mật khẩu tối thiểu phải có 4 chữ cái")
    private String passwords;

    @NotBlank(message = "Tên không được để trống")
    private String firstName;

    @NotBlank(message = "Họ không được để trống")
    private String lastName;

    private Boolean role = false;
}
