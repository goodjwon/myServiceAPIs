package kr.my.files;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class MyApiFilesApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyApiFilesApplication.class, args);
    }
}
