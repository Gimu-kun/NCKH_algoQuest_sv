package com.example.algoQuestSV.Utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class UploadUtils {
    @Value("${file.upload-dir}")
    private String uploadDir;

    public String uploadAvatar(MultipartFile avatarFile, String username) throws IOException {

        if (avatarFile == null || avatarFile.isEmpty()) {
            throw new IllegalArgumentException("File avatar không hợp lệ");
        }

        // Xác định extension an toàn
        String contentType = avatarFile.getContentType();
        String extension = ".jpg"; // mặc định

        if (contentType != null) {
            if (contentType.contains("png")) {
                extension = ".png";
            } else if (contentType.contains("gif")) {
                extension = ".gif";
            }
        }

        String fileName = "avatar_" + username + extension;

        // 🔥 Resolve path đúng cách
        Path uploadPath = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();

        // 🔥 BẮT BUỘC tạo thư mục
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path destination = uploadPath.resolve(fileName);

        // Lưu file
        avatarFile.transferTo(destination.toFile());

        // Trả về path public (dùng cho frontend)
        return "/uploads/" + fileName;
    }
}
