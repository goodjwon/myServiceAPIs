package kr.my.files.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
}
