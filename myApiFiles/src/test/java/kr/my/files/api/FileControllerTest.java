package kr.my.files.api;


import com.fasterxml.jackson.databind.ObjectMapper;
import kr.my.files.dto.FileInfoRequest;
import kr.my.files.dto.FileMetadataResponse;
import kr.my.files.dto.UploadFileRequest;
import kr.my.files.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.test.web.servlet.MvcResult;

import static kr.my.files.enums.UserFilePermissions.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureRestDocs
@SpringBootTest
@AutoConfigureMockMvc
public class FileControllerTest {


    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    FileStorageService fileStorageService;

    private FileInfoRequest fileInfoRequest;


    @DisplayName("file, permission.json 파일 submit 테스트")
    @BeforeAll
    void uploadBefore() throws Exception {
        //Given 파일생성
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt",
                TEXT_PLAIN_VALUE, "Hello, World!".getBytes(StandardCharsets.UTF_8));


        //Json 요청 생성
        List<String> filePermissions = new ArrayList<>();
        filePermissions.add(OWNER_WRITE.getPermission());
        filePermissions.add(OWNER_READ.getPermission());

        List<String> filePermissionGroup = new ArrayList<>();
        filePermissionGroup.add("$2a$10$TuKGiVuLJl3xhaVPDNj3EOcjDyKrMcFcc7m.d.PsFX7UjbTgrl1Ju");
        filePermissionGroup.add("f52fbd32b2b3b86ff88ef6c490628285f482af15ddcb29541f94bcf526a3f6c7");
        filePermissionGroup.add("fb8c2e2b85ca81eb4350199faddd983cb26af3064614e737ea9f479621cfa57a");

        String ownerDomain = "www.abc.com";
        String userCode = "goodjwon@gmail.com";


        FileMetadataResponse response = fileStorageService.saveFile(UploadFileRequest.builder()
                .file(file)
                .fileName(file.getOriginalFilename())
                .userFilePermissions(filePermissions)
                .idAccessCodes(filePermissionGroup)
                .ownerDomainCode(ownerDomain)
                .ownerAuthenticationCode(userCode)
                .build());

        this.fileInfoRequest = FileInfoRequest.builder()
                .filePhyName(response.getFileName())
                .fileCheckSum(response.getCheckSum())
                .ownerAuthenticationCode(response.getOwnerAuthenticationCode())
                .ownerDomainCode(response.getOwnerDomainCode())
                .build();
    }


    @BeforeAll
    void uploadPublicPermissionBefore() throws Exception {
        //Given 파일생성
        File resource = new ClassPathResource("data/sample-image/IMG_3421.jpg").getFile();
        MockMultipartFile file = new MockMultipartFile("image",
                "test.png",
                "image/png",
                new FileInputStream(resource.getAbsoluteFile()));

        //Json 요청 생성
        List<String> filePermissions = new ArrayList<>();
        filePermissions.add(OWNER_WRITE.getPermission());
        filePermissions.add(OWNER_READ.getPermission());
        filePermissions.add(PUBLIC_READ.getPermission());

        String ownerDomain = "www.abc.com";
        String userCode = "goodjwon@gmail.com";

        FileMetadataResponse response = fileStorageService.saveFile(UploadFileRequest.builder()
                .file(file)
                .fileName(file.getOriginalFilename())
                .userFilePermissions(filePermissions)
                .ownerDomainCode(ownerDomain)
                .ownerAuthenticationCode(userCode)
                .build());

        this.fileInfoRequest = FileInfoRequest.builder()
                .filePhyName(response.getFileName())
                .fileCheckSum(response.getCheckSum())
                .ownerAuthenticationCode(response.getOwnerAuthenticationCode())
                .ownerDomainCode(response.getOwnerDomainCode())
                .build();
    }

    @Test
    @DisplayName("file, permission.json 파일 submit 테스트")
    void uploadShouldReturnMetadataNameWithJsonFile() throws Exception {
        //Given 업로드 파일 생성
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt",
                TEXT_PLAIN_VALUE, "Hello, World!".getBytes(StandardCharsets.UTF_8));

        //Given Json 요청 파일 생성
        List<String> filePermissions = new ArrayList<>();
        filePermissions.add(OWNER_WRITE.getPermission());
        filePermissions.add(OWNER_READ.getPermission());
        filePermissions.add(GROUP_READ.getPermission());
        filePermissions.add(GROUP_WRITE.getPermission());

        List<String> filePermissionGroup = new ArrayList<>();
        filePermissionGroup.add("$2a$10$TuKGiVuLJl3xhaVPDNj3EOcjDyKrMcFcc7m.d.PsFX7UjbTgrl1Ju");
        filePermissionGroup.add("f52fbd32b2b3b86ff88ef6c490628285f482af15ddcb29541f94bcf526a3f6c7");
        filePermissionGroup.add("fb8c2e2b85ca81eb4350199faddd983cb26af3064614e737ea9f479621cfa57a");

        String ownerDomain = "www.abc.com";
        String userCode = "goodjwon@gmail.com";

        MockMultipartFile metadata = new MockMultipartFile(
                "metadata",
                "metadata",
                APPLICATION_JSON_VALUE,
                new ObjectMapper()
                        .writeValueAsString(UploadFileRequest.builder()
                                .fileName(file.getOriginalFilename())
                                .userFilePermissions(filePermissions)
                                .idAccessCodes(filePermissionGroup)
                                .ownerDomainCode(ownerDomain)
                                .ownerAuthenticationCode(userCode)
                                .build())
                        .getBytes(StandardCharsets.UTF_8));

        //then http multipart 요청
        mockMvc.perform(multipart("/upload-file-permission-json-file")
                .file(file)      //실제 파일
                .file(metadata)) //요청 설정 파일
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("originFileName").value(file.getOriginalFilename()))
                .andExpect(jsonPath("fileName").exists())
                .andExpect(jsonPath("fileDownloadUri").exists())
                .andExpect(jsonPath("size").exists())
        ;
    }




    @Test
    @DisplayName("이미지 file, permission.json 파일 submit 테스트")
    void uploadImageFileShouldReturnMetadataNameWithJsonFile() throws Exception {
        //Given 파일생성
        File resource = new ClassPathResource("data/sample-image/IMG_3421.jpg").getFile();

        MockMultipartFile file = new MockMultipartFile("file", resource.getName(),
                IMAGE_JPEG_VALUE,
                new FileInputStream(resource.getAbsoluteFile()));

        //Json 요청 생성 - 파일 권한 생성
        List<String> filePermissions = new ArrayList<>();
        filePermissions.add(OWNER_WRITE.getPermission());
        filePermissions.add(OWNER_READ.getPermission());
        filePermissions.add(PUBLIC_READ.getPermission());

        //Json 요청 생성 - 파일 그룹 생성
        List<String> filePermissionGroup = new ArrayList<>();
        filePermissionGroup.add("$2a$10$TuKGiVuLJl3xhaVPDNj3EOcjDyKrMcFcc7m.d.PsFX7UjbTgrl1Ju");
        filePermissionGroup.add("f52fbd32b2b3b86ff88ef6c490628285f482af15ddcb29541f94bcf526a3f6c7");
        filePermissionGroup.add("fb8c2e2b85ca81eb4350199faddd983cb26af3064614e737ea9f479621cfa57a");

        //Json 요청 생성 - 도메인 및 사용자 셋팅
        String ownerDomain = "www.abc.com";
        String userCode = "goodjwon@gmail.com";

        List<Integer> thumbnailWiths = new ArrayList<>();
        thumbnailWiths.add(200);
        thumbnailWiths.add(400);
        thumbnailWiths.add(500);

        MockMultipartFile metadata = new MockMultipartFile(
                "metadata",
                "metadata",
                APPLICATION_JSON_VALUE,
                new ObjectMapper()
                        .writeValueAsString(UploadFileRequest.builder()
                                .fileName(file.getOriginalFilename())
                                .userFilePermissions(filePermissions)
                                .idAccessCodes(filePermissionGroup)
                                .ownerDomainCode(ownerDomain)
                                .ownerAuthenticationCode(userCode)
                                .build())
                        .getBytes(StandardCharsets.UTF_8));
        //then http multipart 요청
        mockMvc.perform(multipart("/upload-file-permission-json-file")
                        .file(file)      //실제 파일
                        .file(metadata)) //요청 설정 파일
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
        //private method test
        Method method = fileStorageService.getClass().getDeclaredMethod("getSubPath", String.class);
        method.setAccessible(true);
        String values = dtf3.format(LocalDateTime.now());

        //when
        String argument = (String) method.invoke(fileStorageService, "yyyy/MM/dd/HH/mm");

        //then
        assertEquals(values, argument);
    }

    @Test
    @DisplayName("파일요청정보를 수신하고 적절한 권한이 부여되어 있으면 정보를 전달 한다.")
    void getFileInfo() throws Exception {
        mockMvc.perform(post("/file-info")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(this.fileInfoRequest)))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                ;
    }

    @Test
    @DisplayName("파일에 대해서 다운로드 정보를 수신하고 파일을 내려 받는다.")
    void downloadFile() throws Exception {
        //given file request info
        //when  file owner check
        //then  file download
        MvcResult result = mockMvc.perform(post("/file-download")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(this.fileInfoRequest)))
                .andExpect(status().is(200))
                .andExpect(header().string("Accept-Ranges", "bytes"))
                .andDo(print())
                .andReturn();

        assertThat(result.getResponse().getStatus(), is(equalTo(200)));
        assertThat(result.getResponse().getContentAsByteArray().length, is(equalTo(13)));
        assertThat(result.getResponse().getContentType(), is(equalTo("application/json;charset=UTF-8")));
    }

    @Test
    @DisplayName("모두 공계 파일일때 별도 지정된 디렉터리로 저장 여부 확인")
    void uploadFPublicFile() throws Exception {
        //given
        //when
        //then
    }

    @Test
    @DisplayName("파일 사용자 정보가 없으면 예외 처리 한다.")
    void saveFileUserinfo() throws Exception {
        //given
        //when
        //then
    }


    @Test
    @DisplayName("파일 메타정보 DB 가 없으면 예외 처리 한다.")
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
    @DisplayName("파일 권한에 열람 가능자 정보를 확인 하고 틀릴경우 401 권한 없음 에러를 던진다. ")
    void groupPermissionHashKeyBadCaseCheck() {
        //given
        //when
        //then
    }

    @Test
    @DisplayName("그룹이 있으면 그룹 권한이 있어야 한다. 그렇지 않을 경우 유효하지 않은 에러 출력")
    void hasGroupMustBeGroupPermission(){
        //given
        //when
        //then
    }


    @Test
    @DisplayName("그룹권한이 있으면 그룹이  있어야 한다. 그렇지 않을 경우 유효하지 않은 에러 출력")
    void hasGroupPermissionMustBeGroup(){
        //given
        //when
        //then
    }






}
