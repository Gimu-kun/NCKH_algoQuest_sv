package com.example.algoQuestSV.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "users")
public class User {
    @Id
    private String id;

    @Length(min = 4, message = "Tên tài khoản tối thiểu phải có 4 chữ cái")
    @NotNull(message = "Tên tài khoản không được để trống!")
    private String username;

    @NotNull(message = "Mật khẩu không được để trống!")
    @Length(min = 4, message = "Mật khẩu tối thiểu phải có 4 chữ cái")
    private String passwords;

    @NotBlank(message = "Tên không được để trống")
    @Column(name = "first_name")
    private String firstName;

    @NotBlank(message = "Họ không được để trống")
    @Column(name = "last_name")
    private String lastName;

    private String avatar = null;

    private Boolean role = false;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @Builder
    public User(String username, String passwords, String firstName, String lastName, String avatar, Boolean role) {
        id = "U-" + UUID.randomUUID().toString().replace("-","").substring(0,5);
        this.username = username;
        this.passwords = passwords;
        this.firstName = firstName;
        this.lastName = lastName;
        this.avatar = avatar;
        this.role = role != null ? role : false;
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) id = "U-" + UUID.randomUUID().toString().replace("-", "").substring(0,5);
        if (createdAt == null) createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
