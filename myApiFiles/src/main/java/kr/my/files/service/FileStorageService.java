package kr.my.files.service;

import kr.my.files.dto.UploadFileMetadataResponse;
import kr.my.files.dto.UploadFileRequest;
import kr.my.files.entity.FilePermissionGroup;
import kr.my.files.entity.MyFiles;
import kr.my.files.enums.FileStatus;
import kr.my.files.enums.UserFilePermissions;
import kr.my.files.exception.FileStorageException;
import kr.my.files.exception.MyFileNotFoundException;
import kr.my.files.property.FileStorageProperties;
import kr.my.files.dao.MyFilesRepository;
import lombok.NoArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.tika.Tika;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import static kr.my.files.enums.UserFilePermissions.OWNER_READ;
import static kr.my.files.enums.UserFilePermissions.OWNER_WRITE;

@NoArgsConstructor
@Service
public class FileStorageService {

    private Path fileStorageLocation;
    private MyFilesRepository myFilesRopository;

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties, MyFilesRepository myFilesRopository) {
        this.fileStorageLocation = Paths
                .get(fileStorageProperties.getUploadDir())
                .toAbsolutePath()
                .normalize();
        this.myFilesRopository = myFilesRopository;
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.");
        }
    }

    /**
     *
     */
    public UploadFileMetadataResponse saveFile(UploadFileRequest fileRequest) {

        String uuidFileName = getUUIDFileName(fileRequest.getFile());
        String subPath = getSubPath("yyyy/MM/dd/HH/mm");
        String savePath = storeFile(fileRequest.getFile(), uuidFileName, subPath);
        String fileDownloadUri = getFileDownloadUri(subPath.concat(uuidFileName));
        String fileHash = getFileHash(fileRequest.getFile());
        MultipartFile file = fileRequest.getFile();

        MyFiles myFile = MyFiles.builder()
                .fileDownloadPath(fileDownloadUri)
                .fileContentType(file.getContentType())
                .fileHashCode(fileHash)
                .fileOrgName(file.getOriginalFilename())
                .filePath(savePath)
                .fileSize(file.getSize())
                .fileStatus(FileStatus.Registered)
                .fileOwnerDisplayName("")
                .userFilePermissions(addDefaultPermission(fileRequest).getUserFilePermissions())
                .filePermissionGroups(addUserAccessCode(fileRequest.getIdAccessCodes()))
                .filePhyName(uuidFileName)
                .postLinkType("")
                .postLinked(0L)
                .fileDownloadPath(fileDownloadUri)
                .build();

        myFilesRopository.save(myFile);

        return UploadFileMetadataResponse.builder().myFiles(myFile).build();

    }

    private List<FilePermissionGroup> addUserAccessCode(List<String> idAccessCode){
        return idAccessCode.stream()
                .map(a ->
                    FilePermissionGroup.builder()
                    .idAccessCode(a)
                    .build())
                .collect(Collectors.toList());
    }

    private UploadFileRequest addDefaultPermission(UploadFileRequest fileRequest) {
        if(fileRequest.getUserFilePermissions().isEmpty()){
            List<UserFilePermissions> filePermissions = new ArrayList<>();
            filePermissions.add(OWNER_WRITE);
            filePermissions.add(OWNER_READ);
            fileRequest.addUserFilePermissions(filePermissions);
        }
        return fileRequest;
    }

    /**
     * 업로드된 파일을 지정된 경로에 저장한다.
     * @param file
     * @return 저장된 경로를 반환한다.
     */
    private String storeFile(MultipartFile file, String uuidFileName, String subPath) {
        // Normalize file name

        try {
            Path targetLocation = this.fileStorageLocation.resolve(subPath); //경로 만들기.

            //경로가 없을 경우 만든다.
            if(!Files.exists(targetLocation)){
                Files.createDirectories(targetLocation);
            }

            Path savePath = targetLocation.resolve(uuidFileName);

            //파일 저장하기
            Files.copy(file.getInputStream(), savePath, StandardCopyOption.REPLACE_EXISTING);

            return savePath.toString();

        } catch (IOException ex) {
            throw new FileStorageException("Could not store file. Please try again!");
        }
    }


    private String getFileDownloadUri(String fullPath){
        return  ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/")
                .path(fullPath)
                .toUriString();
    }


    private String getSubPath(String format){
        DateTimeFormatter dtf3 = DateTimeFormatter.ofPattern(format);
        return dtf3.format(LocalDateTime.now());
    }

    /**
     * 파일명 저장하기.
     * @param file
     * @return uuid 로 파일명이 변경된 파일명 리턴.
     * @throws IOException
     */
    private String getUUIDFileName(MultipartFile file) {
        String ext = FilenameUtils.getExtension(
                StringUtils.cleanPath(file.getOriginalFilename()));
        String uuidFileName = UUID.randomUUID().toString();

        return uuidFileName.concat(".").concat(ext);
    }

    /**
     * file hash 값 찾기 만든가.
     * @param file
     * @return
     * @throws IOException
     */
    private String getFileHash(MultipartFile file)  {
        String digestFileName = "";
        try{
            digestFileName = DigestUtils.md5Hex(file.getInputStream());
        }catch (IOException ioe){
            throw new FileStorageException("file md5 Hash is failed");
        }
        return digestFileName;
    }



    /**
     * 파일 mine type을 확인 한다.
     *
     * @param file
     * @return
     * @throws IOException
     */
    private String getFileMimeType(MultipartFile file) throws IOException {
        String mimeType = new Tika().detect(file.getInputStream());
        return mimeType;
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new MyFileNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new MyFileNotFoundException("File not found " + fileName, ex);
        }
    }
}
