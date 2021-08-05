package kr.my.files.service;

import kr.my.files.dao.FileOwnerRepository;
import kr.my.files.dto.FileInfoRequest;
import kr.my.files.dto.FileMetadataResponse;
import kr.my.files.dto.UploadFileRequest;
import kr.my.files.entity.FileOwner;
import kr.my.files.entity.FilePermissionGroup;
import kr.my.files.entity.MyFiles;
import kr.my.files.enums.FileStatus;
import kr.my.files.exception.FileStorageException;
import kr.my.files.exception.MyFileNotFoundException;
import kr.my.files.exception.OwnerNotMeachedException;
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

import static kr.my.files.commons.utils.StringUtils.stringToChecksum;
import static kr.my.files.enums.UserFilePermissions.OWNER_READ;
import static kr.my.files.enums.UserFilePermissions.OWNER_WRITE;

@NoArgsConstructor
@Service
public class FileStorageService {

    private Path fileStorageLocation;
    private MyFilesRepository myFilesRopository;
    private FileOwnerRepository fileOwnerRepository;

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties,
                              MyFilesRepository myFilesRopository,
                              FileOwnerRepository fileOwnerRepository) {
        this.fileStorageLocation = Paths
                .get(fileStorageProperties.getUploadDir())
                .toAbsolutePath()
                .normalize();
        this.myFilesRopository = myFilesRopository;
        this.fileOwnerRepository = fileOwnerRepository;
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.");
        }
    }

    /**
     *
     */
    public FileMetadataResponse saveFile(UploadFileRequest fileRequest) {

        String uuidFileName = getUUIDFileName(fileRequest.getFile());
        String subPath = getSubPath("yyyy/MM/dd/HH/mm");
        String savePath = storeFile(fileRequest.getFile(), uuidFileName, subPath);
        String fileDownloadUri = getFileDownloadUri(uuidFileName);
        String fileHash = getFileHash(fileRequest.getFile());
        String doaminHash = stringToChecksum(fileRequest.getOwnerDomainCode());
        MultipartFile file = fileRequest.getFile();

        MyFiles myFile = MyFiles.builder()
                .fileDownloadPath(fileDownloadUri)
                .fileContentType(file.getContentType())
                .fileHashCode(fileHash)
                .fileOrgName(file.getOriginalFilename())
                .filePath(savePath)
                .fileSize(file.getSize())
                .fileStatus(FileStatus.Registered)
                .userFilePermissions(addDefaultPermission(fileRequest).getUserFilePermissions())
                .filePermissionGroups(addUserAccessCode(fileRequest.getIdAccessCodes()))
                .filePhyName(uuidFileName)
                .fileOwnerByUserCode(ownerCheckSum(fileRequest))
                .postLinkType("")
                .postLinked(0L)
                .fileDownloadPath(fileDownloadUri)
                .build();

        myFilesRopository.save(myFile);

        return FileMetadataResponse.builder().myFiles(myFile).build();

    }

    /**
     * 파일 유니크 명을통해 파일 정보를 요청한다.
     * @param infoRequest
     * @return
     */
    public FileMetadataResponse getFileInfo(FileInfoRequest infoRequest){

            if(!isOwnerRequest(infoRequest)){
                throw new OwnerNotMeachedException("File not found " + infoRequest.getFilePhyName());
            }
            MyFiles files = myFilesRopository.findByFilePhyNameAndFileHashCode(
                    infoRequest.getFilePhyName(),
                    infoRequest.getFileCheckSum())
                    .orElseThrow(() ->
                            new MyFileNotFoundException("File not found " + infoRequest.getFilePhyName()));

        return FileMetadataResponse.builder().myFiles(files).build();
    }

    /**
     * 파일을 읽어서 스트림으로 돌려준다.
     * @param fileInfoRequest
     * @return
     */
    public Resource loadFileAsResource(FileInfoRequest fileInfoRequest) {
        try {
            if(!isOwnerRequest(fileInfoRequest)){
                throw new OwnerNotMeachedException("File not found " + fileInfoRequest.getFilePhyName());
            }

            MyFiles myFiles = myFilesRopository.findByFilePhyNameAndFileHashCode(
                    fileInfoRequest.getFilePhyName(),
                    fileInfoRequest.getFileCheckSum()).orElseThrow(
                            () -> new MyFileNotFoundException("File not found " + fileInfoRequest.getFilePhyName()));

            Path filePath = this.fileStorageLocation.resolve(myFiles.getFilePath()).normalize();

            Resource resource = new UrlResource(filePath.toUri());

            return resource;

        } catch (MalformedURLException ex) {
            throw new MyFileNotFoundException("File not found " + fileInfoRequest.getFilePhyName());
        }
    }

    private boolean isOwnerRequest(FileInfoRequest fileInfoRequest){
        boolean result = false;
        MyFiles myFiles = myFilesRopository.findByFilePhyNameAndFileHashCode(
                fileInfoRequest.getFilePhyName(),
                fileInfoRequest.getFileCheckSum())
                    .orElseThrow(() -> new MyFileNotFoundException(fileInfoRequest.getFilePhyName()));


        FileOwner fileOwner = fileOwnerRepository.findById(myFiles.getFileOwnerByUserCode().getOwnerSeq())
                    .orElseThrow(()-> new OwnerNotMeachedException(fileInfoRequest.getFilePhyName()));

        boolean a = fileOwner.getOwnerAuthenticationCheckSum().equals(fileInfoRequest.getOwnerAuthenticationCode());
        boolean b = fileOwner.getOwnerDomainCheckSum().equals(fileInfoRequest.getOwnerDomainCode());

        if(a && b) result = true;

        return result;

    }


    private FileOwner ownerCheckSum(UploadFileRequest fileRequest){

        FileOwner fileOwner = ownerInformationConfirmation(fileRequest);

        if(ownerInformationConfirmation(fileRequest) == null){
            fileOwner = fileOwnerRepository.save(FileOwner.builder()
                    .ownerDomainCheckSum(stringToChecksum(fileRequest.getOwnerDomainCode()))
                    .ownerAuthenticationCode(stringToChecksum(fileRequest.getOwnerAuthenticationCode()))
                    .build()
            );
        }

        return fileOwner;
    }

    private FileOwner ownerInformationConfirmation(UploadFileRequest fileRequest){
        FileOwner fileOwner =  fileOwnerRepository
                .findByOwnerDomainCheckSumAndOwnerAuthenticationCheckSum(
                        stringToChecksum(fileRequest.getOwnerDomainCode()),
                        stringToChecksum(fileRequest.getOwnerAuthenticationCode())).orElse(null);
        return fileOwner;
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
            List<String> filePermissions = new ArrayList<>();
            filePermissions.add(OWNER_WRITE.getPermission());
            filePermissions.add(OWNER_READ.getPermission());
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

    //todo 환경변수로 처리 할 수 있도록 수정.
    private String getFileDownloadUri(String fullPath){
        return "/file-download/".concat(fullPath);
    }

    /**
     * 요청받은 형태의 시간으로
     * @param format
     * @return
     */
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


}
