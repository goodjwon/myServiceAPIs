package kr.my.files.api;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UtilTest {

    @Test
    @DisplayName("파일 저장시 일자에 맞는 디렉터리 구조로 저장 되는지 확인")
    void checkFileSavePath() throws Exception {
        //given
        DateTimeFormatter dtf3 = DateTimeFormatter.ofPattern("yyyy/MM/dd/HH/mm/");
        String result = dtf3.format(LocalDateTime.now());
        System.out.println(result);
        //then
        assertEquals(dtf3.format(LocalDateTime.now()), result);
    }

    @Test
    void getFileHashCode() throws IOException {
//        File resource = new ClassPathResource("data/sample-pdf/S2B20190929-G00009.pdf").getFile();

        File resource2 = new ClassPathResource("data/sample-image/IMG_3421.jpg").getFile();

        ClassPathResource resource3 = new ClassPathResource("data/sample-pdf/aaa111.pdf");

        InputStream inputStream = resource3.getInputStream();

        byte file[] = inputStream.readAllBytes();

        System.out.println(file);


        System.out.println(resource3);
    }
}
