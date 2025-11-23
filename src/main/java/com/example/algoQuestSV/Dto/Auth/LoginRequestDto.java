package com.example.algoQuestSV.Dto.Auth;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class LoginRequestDto {
    @Length(min = 4, message = "Tên tài khoản tối thiểu phải có 4 chữ cái")
    @NotNull(message = "Tên tài khoản không được để trống!")
    private String username;

    @NotNull(message = "Mật khẩu không được để trống!")
    @Length(min = 4, message = "Mật khẩu tối thiểu phải có 4 chữ cái")
    private String passwords;
}
