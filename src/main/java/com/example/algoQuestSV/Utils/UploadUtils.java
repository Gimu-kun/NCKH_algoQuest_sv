package com.example.algoQuestSV.Utils;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@Component
public class UploadUtils {
    private String uploadDir = "D:/uploads/";

    public String uploadAvatar(MultipartFile avatarFile, String username){
        try {
            if (avatarFile.isEmpty()) {
                throw new IllegalArgumentException("File avatar không hợp lệ.");
            }

            String fileExtension = ".jpg"; // Đặt mặc định là .jpg
            String mimeType = avatarFile.getContentType();

            if (mimeType != null) {
                if (mimeType.contains("png")) {
                    fileExtension = ".png";
                } else if (mimeType.contains("gif")) {
                    fileExtension = ".gif";
                }
            }

            String uniqueFileName = "avatar_" + username + fileExtension;

            // Lưu file vào thư mục upload
            File uploadDirFile = new File(uploadDir);
            if (!uploadDirFile.exists()) {
                uploadDirFile.mkdirs();
            }

            Path dest = Paths.get(uploadDir + File.separator + uniqueFileName);
            avatarFile.transferTo(dest.toFile());

            return "/uploads/" + uniqueFileName;
        } catch (Exception e) {
            // Log lỗi và trả về thông báo lỗi
            e.printStackTrace();
            return "Có lỗi xảy ra khi tải ảnh lên";
        }
    }
}
