package vn.edu.fpt.golden_chicken.controllers.client;

import java.io.IOException;
import java.net.URISyntaxException;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/file")
public class FileController {
    public String uploadFile(@RequestPart("product") MultipartFile file) throws URISyntaxException, IOException {
        return "1";
    }
}
