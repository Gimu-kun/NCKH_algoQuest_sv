package com.example.algoQuestSV.Entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
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

    private Integer level = 1;

    private Integer exp = 0;

    private Integer woods = 0;

    private Integer stones = 0;

    private Integer point = 100;

    private Integer gold = 0;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Streak> streaks;

    @PrePersist
    protected void onCreate() {
        if (id == null) id = "U-" + UUID.randomUUID().toString().replace("-", "").trim().substring(0,6);
        if (createdAt == null) createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
