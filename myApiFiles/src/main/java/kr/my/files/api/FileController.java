package kr.my.files.api;

import kr.my.files.dto.FileInfoRequest;
import kr.my.files.dto.FileMetadataResponse;
import kr.my.files.dto.FilePermissionAddRequest;
import kr.my.files.dto.UploadFileRequest;
import kr.my.files.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.IanaLinkRelations;
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
@RequiredArgsConstructor
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);
    private final FileStorageService fileStorageService;
    private final FileAssembler fileAssembler;

    /**
     * Form 과 json 파일로 요청
     *
     * @param file
     * @param fileRequest
     * @return
     */
    @ExceptionHandler(value = IOException.class)
    @PostMapping(value = "/upload-file-permission-json-file" )
    public ResponseEntity<FileMetadataResponse> uploadFileAndPerMissionWithJsonFile(
            @RequestPart(value = "file") MultipartFile file,
            @RequestPart(value = "metadata") UploadFileRequest fileRequest) {

        fileRequest.addFile(file);
        fileRequest.addFileName(file.getOriginalFilename());

        FileMetadataResponse fileMetadataResponse
                = fileStorageService.saveFile(fileRequest);

        fileMetadataResponse = fileAssembler.toModel(fileMetadataResponse);


        return ResponseEntity.created(fileMetadataResponse.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(fileMetadataResponse);
    }

    @PostMapping("/file-permission-add")
    public ResponseEntity<FileMetadataResponse> addFilePermissionGroups(
            @RequestBody FilePermissionAddRequest request) {
        FileMetadataResponse fileMetadataResponse = fileStorageService.addFilePermission(request);

        fileMetadataResponse = fileAssembler.toModel(fileMetadataResponse);

        return ResponseEntity.created(fileMetadataResponse.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(fileMetadataResponse);
    }

    /**
     * 파일을 정보를 요청한다.
     */
    @PostMapping("/file-info")
    public ResponseEntity<FileMetadataResponse> fileInfo(
            @RequestBody @Valid FileInfoRequest fileInfoRequest) {
        FileMetadataResponse fileMetadataResponse = fileStorageService.getFileInfo(fileInfoRequest);

        return ResponseEntity.ok(fileAssembler.toModel(fileMetadataResponse));
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

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
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

}