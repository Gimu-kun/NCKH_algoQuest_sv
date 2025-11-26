package com.example.algoQuestSV.Service;

import com.example.algoQuestSV.Dto.Api.ApiResponseDto;
import com.example.algoQuestSV.Dto.Auth.LoginRequestDto;
import com.example.algoQuestSV.Dto.User.UserCreationDto;
import com.example.algoQuestSV.Dto.User.UserUpdateDto;
import com.example.algoQuestSV.Entity.User;
import com.example.algoQuestSV.Repository.UsersRepository;
import com.example.algoQuestSV.Utils.JwtUtils;
import com.example.algoQuestSV.Utils.PasswordsUtils;
import com.example.algoQuestSV.Utils.UploadUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    UsersRepository usersRepository;

    @Autowired
    PasswordsUtils passwordsUtils;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    UploadUtils uploadUtils;

    @Autowired
    StreakService streakService;

    public ApiResponseDto<User> createUser(UserCreationDto req, MultipartFile avatar){
        if (usersRepository.existsByUsername(req.getUsername())){
            return ApiResponseDto.<User>builder()
                    .status(409)
                    .message("Tên tài khoản đã tồn tại!")
                    .data(null)
                    .build();
        }

        try{
            String avatarPath = "https://www.google.com/url?sa=i&url=https%3A%2F%2Fvdostavka.ru%2Fno-avatar%2F&psig=AOvVaw0HO_umZAEBEgRvvwOOVj0b&ust=1764241620525000&source=images&cd=vfe&opi=89978449&ved=0CBIQjRxqFwoTCLCXxqzWj5EDFQAAAAAdAAAAABAE";
            if (!avatar.isEmpty()){
                avatarPath = uploadUtils.uploadAvatar(avatar,req.getUsername());
            }

            User user = User.builder()
                    .username(req.getUsername())
                    .passwords(passwordsUtils.hashPassword(req.getPasswords()))
                    .firstName(req.getFirstName().trim())
                    .lastName(req.getLastName().trim())
                    .avatar(avatarPath)
                    .role(req.getRole())
                    .build();

            usersRepository.save(user);
            return ApiResponseDto.<User>builder()
                    .status(201)
                    .message("Tạo tài khoản thành công!")
                    .data(user)
                    .build();
        } catch (Exception e) {
            return ApiResponseDto.<User>builder()
                    .status(400)
                    .message("Tạo tài khoản không thành công!")
                    .data(null)
                    .build();
        }
    };

    public ApiResponseDto<List<User>> getAll(){
        return ApiResponseDto.<List<User>>builder()
                .status(200)
                .message("Lấy danh sách thành công!")
                .data(usersRepository.findAll())
                .build();
    }

    public ApiResponseDto<String> login(LoginRequestDto req){
        Optional<User> optUser = usersRepository.findByUsername(req.getUsername());
        if (optUser.isEmpty()){
            return ApiResponseDto.<String>builder()
                    .status(404)
                    .message("Tài khoản không tồn tại!")
                    .data(null)
                    .build();
        }
        User user = optUser.get();
        if (!passwordsUtils.checkPassword(req.getPasswords(),user.getPasswords())){
            return ApiResponseDto.<String>builder()
                    .status(401)
                    .message("Mật khẩu không đúng!")
                    .data(null)
                    .build();
        }
        String token = jwtUtils.createToken(user);
        //Kiểm tra streak khi đang nhập
        streakService.checkUpdate(user);
        return ApiResponseDto.<String>builder()
                .status(200)
                .message("Đăng nhập thành công!")
                .data(token)
                .build();
    }

    public ApiResponseDto<User> update(UserUpdateDto req, String id, MultipartFile avatar) {
        Optional<User> optUser = usersRepository.findById(id);
        if (optUser.isEmpty()){
            return ApiResponseDto.<User>builder()
                    .status(404)
                    .message("Không tìm thấy tài khoản tương ứng!")
                    .data(null)
                    .build();
        }

        User user = optUser.get();

        if(!passwordsUtils.checkPassword(req.getConfirmPasswords(), user.getPasswords())){
            return ApiResponseDto.<User>builder()
                    .status(401)
                    .message("Mật khẩu xác thực không đúng!")
                    .data(null)
                    .build();
        }

        if (req.getFirstName() != null){
            user.setFirstName(req.getFirstName());
        }

        if (req.getLastName() != null){
            user.setLastName(req.getLastName());
        }

        if (req.getRole() != null){
            user.setRole(req.getRole());
        }

        if (req.getPasswords() != null){
            user.setPasswords(passwordsUtils.hashPassword(req.getPasswords()));
        }

        String avatarPath = null;
        if (avatar != null){
            avatarPath = uploadUtils.uploadAvatar(avatar,user.getUsername());
            user.setAvatar(avatarPath);
        }

        usersRepository.save(user);
        return ApiResponseDto.<User>builder()
                .status(200)
                .message("Cập nhật thông tin thành công!")
                .data(user)
                .build();
    }

    public ApiResponseDto<User> getUserById(String id) {
        Optional<User> optUser = usersRepository.findById(id);
        if(optUser.isEmpty()){
            return ApiResponseDto.<User>builder()
                    .status(404)
                    .message("Không tìm thấy người dùng với id là" + id +" !")
                    .data(null)
                    .build();
        }
        return ApiResponseDto.<User>builder()
                .status(200)
                .message("Lấy thông tin người dùng thành công!")
                .data(optUser.get())
                .build();
    }
}
