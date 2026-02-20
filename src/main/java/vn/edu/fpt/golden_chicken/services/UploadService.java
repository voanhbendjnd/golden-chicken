package vn.edu.fpt.golden_chicken.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UploadService {

    public UploadService() {
    }

    public String handleSaveUploadFile(MultipartFile file, String targetFolder) {
        try {
            if (file == null || file.isEmpty()) {
                return "";
            }

            Path sourceDirectory = Paths.get("src", "main", "resources", "static", targetFolder).toAbsolutePath();
            Path runtimeDirectory = Paths.get("target", "classes", "static", targetFolder).toAbsolutePath();
            Files.createDirectories(sourceDirectory);
            Files.createDirectories(runtimeDirectory);

            String originalName = file.getOriginalFilename();
            if (!StringUtils.hasText(originalName)) {
                originalName = "avatar.png";
            }

            String cleanedName = StringUtils.cleanPath(originalName);
            String finalName = System.currentTimeMillis() + "-" + cleanedName;
            byte[] content = file.getBytes();

            Path sourcePath = sourceDirectory.resolve(finalName);
            Path runtimePath = runtimeDirectory.resolve(finalName);
            Files.write(sourcePath, content);
            Files.write(runtimePath, content);
            return finalName;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
