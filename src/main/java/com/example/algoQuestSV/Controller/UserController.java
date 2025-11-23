package com.example.algoQuestSV.Controller;

import com.example.algoQuestSV.Dto.Api.ApiResponseDto;
import com.example.algoQuestSV.Dto.Auth.LoginRequestDto;
import com.example.algoQuestSV.Dto.User.UserCreationDto;
import com.example.algoQuestSV.Dto.User.UserUpdateDto;
import com.example.algoQuestSV.Entity.User;
import com.example.algoQuestSV.Service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("api/users")
@CrossOrigin("*")
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<User>> register(@Valid @ModelAttribute UserCreationDto req, @RequestPart("avatar") MultipartFile avatar){
        ApiResponseDto<User> result = userService.createUser(req,avatar);
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<List<User>>> getList(){
        return ResponseEntity.ok(userService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<User>> getUserById(@PathVariable String id){
        ApiResponseDto<User> result = userService.getUserById(id);
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto<String>> login(@Valid @RequestBody LoginRequestDto req){
        ApiResponseDto<String> result = userService.login(req);
        return ResponseEntity.status(result.getStatus()).body(result);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponseDto<User>> update(@Valid @ModelAttribute UserUpdateDto req, @RequestPart("avatar") MultipartFile avatar, @PathVariable String id){
        ApiResponseDto<User> result = userService.update(req,id,avatar);
        return ResponseEntity.status(result.getStatus()).body(result);
    }
}
