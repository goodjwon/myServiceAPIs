package kr.my.files.api;


import com.fasterxml.jackson.databind.ObjectMapper;
import kr.my.files.dto.UploadFileRequest;
import kr.my.files.enums.UserFilePermissions;
import kr.my.files.service.FileStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static kr.my.files.enums.UserFilePermissions.OWNER_READ;
import static kr.my.files.enums.UserFilePermissions.OWNER_WRITE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureMockMvc
public class FileControllerTest {


    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    FileStorageService fileStorageService;

    @Test
    @DisplayName("file, permission.json 파일 submit 테스트")
    void uploadShouldReturnMetadataNameWithJsonFile() throws Exception {
        //Given 파일생성
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt",
                TEXT_PLAIN_VALUE, "Hello, World!".getBytes(StandardCharsets.UTF_8));

        //Given Json 파일 생성
        //Json 요청 생성
        List<UserFilePermissions> filePermissions = new ArrayList<>();
        filePermissions.add(OWNER_WRITE);
        filePermissions.add(OWNER_READ);

        List<String> filePermissionGroup = new ArrayList<>();
        filePermissionGroup.add("$2a$10$TuKGiVuLJl3xhaVPDNj3EOcjDyKrMcFcc7m.d.PsFX7UjbTgrl1Ju");
        filePermissionGroup.add("f52fbd32b2b3b86ff88ef6c490628285f482af15ddcb29541f94bcf526a3f6c7");
        filePermissionGroup.add("fb8c2e2b85ca81eb4350199faddd983cb26af3064614e737ea9f479621cfa57a  ");

        MockMultipartFile metadata = new MockMultipartFile(
                "metadata",
                "metadata",
                APPLICATION_JSON_VALUE,
                new ObjectMapper()
                        .writeValueAsString(UploadFileRequest.builder()
                                .fileName(file.getOriginalFilename())
                                .userFilePermissions(filePermissions)
                                .idAccessCodes(filePermissionGroup)
                                .build())
                        .getBytes(StandardCharsets.UTF_8));

        //then
        mockMvc.perform(multipart("/upload-file-permission-json-file")
                .file(file).file(metadata))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("originFileName").value(file.getOriginalFilename()))
                .andExpect(jsonPath("fileName").exists())
                .andExpect(jsonPath("fileDownloadUri").exists())
                .andExpect(jsonPath("size").exists())
        ;
    }

    @Test
    @DisplayName("파일 저장시 일자에 맞는 디렉터리 구조로 저장 되는지 확인")
    void checkFileSavePath() throws Exception {
        //given
        DateTimeFormatter dtf3 = DateTimeFormatter.ofPattern("yyyy/MM/dd/HH/mm");
        Method method = fileStorageService.getClass().getDeclaredMethod("getSubPath", String.class);
        method.setAccessible(true);
        String values = dtf3.format(LocalDateTime.now());

        //when
        String argument = (String) method.invoke(fileStorageService, "yyyy/MM/dd/HH/mm");

        //then
        assertEquals(values, argument);
    }

    @Test
    @DisplayName("모두 공계 파일일때 별도 지정된 디렉터리로 저장 여부 확인")
    void uploadFPublicFile() throws Exception {
        //given
        //when
        //then
    }

    @Test
    @DisplayName("파일 사용자 정보를 DB에 입력 한다.")
    void saveFileUserinfo() throws Exception {
        //given
        //when
        //then
    }


    @Test
    @DisplayName("파일 메타정보 DB 입력")
    void saveFileMetainfo() throws Exception {
        //given
        //when
        //then
    }

    @Test
    @DisplayName("파일요청서 퍼미션에 대당 하지 않으면 정보 열람을 거부한다.")
    void checkFilePermission() throws Exception {
        //given
        //when
        //then
    }

    @Test
    @DisplayName("업로드된 파일에 대해서 파일 다운로드 요청 관련 정보를 던지면 파일을 다운로드 한다.")
    void downloadFile() throws Exception {
        //given
        //when
        //then
    }

    @Test
    @DisplayName("업로드된 파일에 대해서 파일 다운로드 요청 요청정보를 던지면 소유주만 파일을 다운로드 한다.")
    void downloadFilecheckFilePermission() throws Exception {
        //given
        //when
        //then
    }



    @Test
    @DisplayName("파일 다운로드 기록 저장")
    void saveFileDownloadPath() throws Exception {
        //given
        //when
        //then

    }

    @Test
    @DisplayName("파일 권한에 열람 가능자 정보를 저장한다. ")
    void testPermission() {
        //given
        //when
        //then
    }

    @Test
    @DisplayName("파일 권한에 열람 가능자 정보가 있으면 맞는지 확인 하고 다운로드 시킨다. ")
    void groupPermissionHashKeyCorrectCaseCheck() {
        //given
        //when
        //then
    }

    @Test
    @DisplayName("파일 권한에 열람 가능자 정보를 확인 하고 틀릴경우 401 권한 없음 에러를 던진다. ")
    void groupPermissionHashKeyBadCaseCheck() {
        //given
        //when
        //then
    }



}
