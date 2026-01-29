package vn.edu.fpt.golden_chicken.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

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

}
