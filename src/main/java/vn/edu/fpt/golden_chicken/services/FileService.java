package vn.edu.fpt.golden_chicken.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import vn.edu.fpt.golden_chicken.common.DefineVariable;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class FileService {
    @Value("${djnd.upload-file.base-uri}")
    private String baseURI;

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

    public void initFolder(String folderName) throws URISyntaxException, IOException {
        var path = Paths.get(baseURI, folderName);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
            System.out.println(">>> CREATE NEW DIRECTORY SUCCESSFULLY WITH PATH (" + path + ")");
        } else {
            System.out.println(">>> SKIP MAKING DIRECTORY, ALREADY EXISTS");
        }
    }

    public String getLastNameFile(MultipartFile file) throws URISyntaxException, IOException {
        var upPath = baseURI + DefineVariable.productFolder;
        var directory = Paths.get(upPath);
        Files.createDirectories(directory);
        var firstName = file.getOriginalFilename();
        if (firstName == null) {
            firstName = "noname";
        }
        var lastName = System.currentTimeMillis() + "-" + StringUtils.cleanPath(firstName);
        var filePath = directory.resolve(lastName);
        try (InputStream is = file.getInputStream()) {
            Files.copy(is, filePath, StandardCopyOption.REPLACE_EXISTING);
        }
        return lastName;
    }

    public void deleteFile(String fileName) throws IOException {
        if (fileName != null && !fileName.isEmpty()) {
            var path = Paths.get(baseURI + fileName);
            Files.deleteIfExists(path);
        }
        return;
    }

}
