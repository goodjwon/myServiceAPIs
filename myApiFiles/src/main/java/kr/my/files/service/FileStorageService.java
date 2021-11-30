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
import kr.my.files.exception.OverImagePixelException;
import kr.my.files.exception.OwnerNotMeachedException;
import kr.my.files.property.FileStorageProperties;
import kr.my.files.dao.MyFilesRepository;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.codec.digest.DigestUtils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
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

import javax.imageio.ImageIO;

import static kr.my.files.commons.utils.StringUtils.collectionToStream;
import static kr.my.files.commons.utils.StringUtils.makeMD5StringToChecksum;
import static kr.my.files.enums.UserFilePermissions.*;

@NoArgsConstructor
@Service
@Slf4j
public class FileStorageService {

    private Path fileStorageLocation;
    private MyFilesRepository myFilesRopository;
    private FileOwnerRepository fileOwnerRepository;
    private FileStorageProperties fileStorageProperties;

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties,
                              MyFilesRepository myFilesRopository,
                              FileOwnerRepository fileOwnerRepository) {

        this.myFilesRopository = myFilesRopository;
        this.fileOwnerRepository = fileOwnerRepository;
        this.fileStorageProperties = fileStorageProperties;

    }

    public void saveThumbnailImage(MyFiles parentFile, List<Integer> thumbnailSizeList){
        parentFile.getFilePath();
        parentFile.getFileDownloadPath();
        parentFile.getFileContentType();
        parentFile.getFilePhyName();
        parentFile.getFileOwnerByUserCode();
        parentFile.getFileStatus();
        parentFile.getFileDownloadPath();

        MyFiles subFileCommon = MyFiles.builder().build();

        File rootImge = new File(parentFile.getFilePath());

        thumbnailSizeList.stream().forEach(i->{
            System.out.println(i);

            try {
                resizeImage(ImageIO.read(rootImge) , i, i, "jpg" );
            } catch (Exception e) {
                e.printStackTrace();
            }


//            myFilesRopository.save(myFile);
        });

//        myFilesRopository.save(null);
    }

    /**
     *
     */
    public FileMetadataResponse saveFile(UploadFileRequest fileRequest) {

        String uuidFileName = getUUIDFileName(fileRequest.getFile());
        String subPath = getSubPath("yyyy/MM/dd/HH/mm");
        String savePath = storeFile(fileRequest, uuidFileName, subPath);
        String fileDownloadUri = getFileDownloadUri(fileRequest, uuidFileName);
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
                .userFilePermissions(addDefaultPermission(fileRequest).getUserFilePermissions())
                .filePermissionGroups(addUserAccessCode(fileRequest.getIdAccessCodes()))
                .filePhyName(uuidFileName)
                .fileOwnerByUserCode(ownerCheckSum(fileRequest))
                .postLinkType("")
                .postLinked(0L)
                .build();

        myFilesRopository.save(myFile);

        if(fileRequest.getThumbnailWiths() !=null && fileRequest.getThumbnailWiths().stream().count() > 0 ){
            saveThumbnailImage(myFile, fileRequest.getThumbnailWiths());
        }

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

    /**
     * 소유자의 요청인지 확인
     * @param fileInfoRequest
     * @return
     */
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
                    .ownerDomainCheckSum(makeMD5StringToChecksum(fileRequest.getOwnerDomainCode()))
                    .ownerAuthenticationCode(makeMD5StringToChecksum(fileRequest.getOwnerAuthenticationCode()))
                    .build()
            );
        }

        return fileOwner;
    }

    private FileOwner ownerInformationConfirmation(UploadFileRequest fileRequest){
        FileOwner fileOwner =  fileOwnerRepository
                .findByOwnerDomainCheckSumAndOwnerAuthenticationCheckSum(
                        makeMD5StringToChecksum(fileRequest.getOwnerDomainCode()),
                        makeMD5StringToChecksum(fileRequest.getOwnerAuthenticationCode())).orElse(null);
        return fileOwner;
    }

    /**
     * 추가로 access 할 수 있는 그룹을 지정한다.
     * @param idAccessCode
     * @return
     */
    private List<FilePermissionGroup> addUserAccessCode(List<String> idAccessCode){
        return collectionToStream(idAccessCode)
                .map(a ->
                        FilePermissionGroup.builder()
                                .idAccessCode(a)
                                .build())
                .collect(Collectors.toList());
    }

    /**
     * 기본으로 올린사람의 권한은 보장한다.
     * @param fileRequest
     * @return
     */
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
     * @param request
     * @return 저장된 경로를 반환한다.
     */
    private String storeFile(UploadFileRequest request, String uuidFileName, String subPath) {
        try {

            String rootPath = isPublicPermission(request)?fileStorageProperties.getPublicSpaceDir():fileStorageProperties.getUploadDir();
            this.fileStorageLocation = Paths.get(rootPath)
                    .toAbsolutePath()
                    .normalize();

            Path targetLocation = this.fileStorageLocation.resolve(subPath); //경로 만들기.

            //경로가 없을 경우 만든다.
            if(!Files.exists(targetLocation)){
                Files.createDirectories(targetLocation);
            }

            Path savePath = targetLocation.resolve(uuidFileName);

            //파일 저장하기
            Files.copy(request.getFile().getInputStream(), savePath, StandardCopyOption.REPLACE_EXISTING);

            return savePath.toString();

        } catch (IOException ex) {
            throw new FileStorageException("Could not store file. Please try again!");
        }
    }

    /**
     * 업로드 파일이 이미지 파일경우 리사이즈 버전을 만든다.
     */
    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight, String imageFormat) throws Exception {


        /**
         * jpg, jpeg, png 는 아래와 같은 이미지 특징을 가진단다. (인터넷)
         */
        if (originalImage.getType() == BufferedImage.TYPE_INT_ARGB
                || originalImage.getType() == BufferedImage.TYPE_INT_ARGB_PRE
                || originalImage.getType() == BufferedImage.TYPE_3BYTE_BGR
                || originalImage.getType() == BufferedImage.TYPE_BYTE_GRAY
                || originalImage.getType() == BufferedImage.TYPE_BYTE_INDEXED

        ) {
            //
            log.info("inside if ");
        }
        log.info(String.valueOf(originalImage.getType()));
        log.info(String.valueOf(originalImage.getWidth()));

        if( originalImage.getWidth() > 5000){
            throw new OverImagePixelException("3840 pixel over");
        }

        if( originalImage.getHeight() > 5000){
            throw new OverImagePixelException("3840 pixel over");
        }


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Thumbnails.of(originalImage)
                .size(targetWidth, targetHeight)
                .outputFormat(imageFormat)
                .outputQuality(1)
                .toOutputStream(outputStream);
        byte[] data = outputStream.toByteArray();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        return ImageIO.read(inputStream);
    }


    /**
     * 요청에 public 인자가 있는지 점검한다.
     * @param request
     * @return public 인지 아닌지 리턴
     */
    private boolean isPublicPermission(UploadFileRequest request){
        final boolean[] result = {false};
        request.getUserFilePermissions().forEach(filePermission->{
            if(filePermission.equals(PUBLIC_READ.getPermission())){
                result[0] = true;
            }
        });

        return result[0];
    }

    /**
     * download path를 정한다.
     * @param request
     * @param fullPath
     * @return
     */
    private String getFileDownloadUri(UploadFileRequest request, String fullPath){
        String downloadPath = isPublicPermission(request)?
                this.fileStorageProperties.getDownloadPublicPath().concat(fullPath):
                this.fileStorageProperties.getDownloadPath().concat(fullPath);

        return downloadPath;
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
     * @param file
     * @return
     * @throws IOException
     */
    private String getFileMimeType(MultipartFile file) throws IOException {
        String mimeType = new Tika().detect(file.getInputStream());
        return mimeType;
    }

}
