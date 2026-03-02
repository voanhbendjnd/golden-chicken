package vn.edu.fpt.golden_chicken;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
// @SpringBootApplication(exclude =
// org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class)
public class GoldenChickenApplication {

	public static void main(String[] args) {
		SpringApplication.run(GoldenChickenApplication.class, args);
	}

}
