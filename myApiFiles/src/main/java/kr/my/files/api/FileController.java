package kr.my.files.api;

import kr.my.files.dto.UploadFileMetadataResponse;
import kr.my.files.dto.UploadFileRequest;
import kr.my.files.enums.UserFilePermissions;
import kr.my.files.exception.FileStorageException;
import kr.my.files.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static kr.my.files.enums.UserFilePermissions.OWNER_READ;
import static kr.my.files.enums.UserFilePermissions.OWNER_WRITE;

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
    public ResponseEntity<UploadFileMetadataResponse> uploadFileAndPerMissionWithJsonFile(
            @RequestPart(value = "file") MultipartFile file,
            @RequestPart(value = "metadata") UploadFileRequest fileRequest) {

        fileRequest.addFile(file);
        fileRequest.addFileName(file.getOriginalFilename());

        UploadFileMetadataResponse fileMetadataResponse
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
    public ResponseEntity<UploadFileMetadataResponse> uploadMultipleFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestPart(value = "metadata", required = false) UploadFileRequest fileRequest) {

        fileRequest.addFiles(files);

         Arrays.asList(files)
                .stream()
                .map(file -> fileStorageService.saveFile(fileRequest))
                .collect(Collectors.toList());


         return null;
    }


    @GetMapping("/downloadFile/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        // Load file as Resource
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
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