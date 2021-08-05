package kr.my.files.api;

import kr.my.files.dto.FileInfoRequest;
import kr.my.files.dto.FileMetadataResponse;
import kr.my.files.dto.UploadFileRequest;
import kr.my.files.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    private final FileStorageService fileStorageService;

    private final FileAssembler fileAssembler;

    public FileController(FileStorageService fileStorageService, FileAssembler fileAssembler) {
        this.fileStorageService = fileStorageService;
        this.fileAssembler = fileAssembler;
    }

    /**
     * Form 과 json 파일로 요청
     *
     * @param file
     * @param fileRequest
     * @return
     */
    @PostMapping(value = "/upload-file-permission-json-file")
    public ResponseEntity<FileMetadataResponse> uploadFileAndPerMissionWithJsonFile(
            @RequestPart(value = "file") MultipartFile file,
            @RequestPart(value = "metadata") UploadFileRequest fileRequest) {

        fileRequest.addFile(file);
        fileRequest.addFileName(file.getOriginalFilename());

        FileMetadataResponse fileMetadataResponse
                = fileStorageService.saveFile(fileRequest);


        return ResponseEntity.ok(fileMetadataResponse);
    }

    /**
     * TODO 다중파일 저장. 작업 중.
     *
     * @param files
     * @return
     */
    @PostMapping("/upload-files-permission")
    public ResponseEntity<FileMetadataResponse> uploadMultipleFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestPart(value = "metadata", required = false) UploadFileRequest fileRequest) {

        fileRequest.addFiles(files);

         Arrays.asList(files)
                .stream()
                .map(file -> fileStorageService.saveFile(fileRequest))
                .collect(Collectors.toList());


         return null;
    }

    /**
     * 파일을 정보를 요청한다.
     */
    @PostMapping("/file-info")
    public ResponseEntity<FileMetadataResponse> fileInfo(
            @RequestBody @Valid FileInfoRequest fileInfoRequest) {
        FileMetadataResponse response = fileStorageService.getFileInfo(fileInfoRequest);

        return ResponseEntity.ok(response);
    }



    /**
     * 파일을 정보를 전달 해서 파일을 다운로드 받는다.
     * @param fileInfoRequest
     * @return
     */
    @PostMapping("/file-download")
    public ResponseEntity<Resource> fileDownLoad(
            @RequestBody @Valid FileInfoRequest fileInfoRequest){
        // Load file as Resource
        Resource resource = fileStorageService.loadFileAsResource(fileInfoRequest);

        // Try to determine file's content type
        String contentType = null;
        try {
            File file = resource.getFile();
            URLConnection connection = file.toURI().toURL().openConnection();
            contentType = connection.getContentType();

        } catch (IOException ex) {
            logger.info("Could not determine file type.");
        }

        // Fallback to the default content type if type could not be determined
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

}