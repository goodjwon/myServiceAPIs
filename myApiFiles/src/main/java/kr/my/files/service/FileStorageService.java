package kr.my.files.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;

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
import org.apache.commons.io.IOUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.codec.digest.DigestUtils;

import java.awt.image.BufferedImage;
import java.io.*;
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
import org.springframework.web.multipart.commons.CommonsMultipartFile;

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

    public void saveThumbnailImage(MyFiles parentFile, InputStream file, List<Integer> thumbnailSizeList){
        File rootImage = new File(parentFile.getFilePath());
        String subPath = getSubPath("yyyy/MM/dd/HH/mm");

        thumbnailSizeList.stream().forEach(i->{
            log.info("########################################################");
            String uuidFileName = getThumbnailName(rootImage.getName(), i.toString());
            String savePath = storeFile(file, parentFile.getUserFilePermissions(), uuidFileName, subPath);
            String fileDownloadUri = getFileDownloadUri(parentFile.getUserFilePermissions(), uuidFileName);
            File outImage = new File(savePath);

            System.out.println(i);

            try {
                outImage= resizeImage(rootImage , outImage, i, 0, "jpg" );

                List<String> filePermissionGroups = parentFile.getFilePermissionGroups().stream()
                        .map(a -> a.getIdAccessCode())
                        .collect(Collectors.toList());

                MyFiles subFileCommon = MyFiles.builder()
                        .fileDownloadPath(fileDownloadUri)
                        .fileContentType(parentFile.getFileContentType())
                        .fileHashCode(getFileHash(outImage))
                        .fileOrgName(parentFile.getFilePhyName())
                        .filePath(savePath)
                        .fileSize(outImage.length())
                        .fileStatus(FileStatus.Registered)
                        .userFilePermissions(addDefaultPermission()) //에러남   새로 생성홰서 처리 필요. 상위 객체 참조 불가.
                        .filePermissionGroups(addUserAccessCode(filePermissionGroups))   //에러남, 새로 생성해서 처리 필요 상위 객체 참조 불가.
                        .fileOwnerByUserCode(parentFile.getFileOwnerByUserCode())
                        .filePhyName(uuidFileName)
                        .postLinkType("")
                        .postLinked(0L)
                        .build();

                myFilesRopository.save(subFileCommon);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     *
     */
    public FileMetadataResponse saveFile(UploadFileRequest fileRequest) {
        try {
            String uuidFileName = getUUIDFileName(fileRequest.getFile().getOriginalFilename());
            String subPath = getSubPath("yyyy/MM/dd/HH/mm");
            String savePath = storeFile(fileRequest.getFile().getInputStream(), fileRequest.getUserFilePermissions(), uuidFileName, subPath);
            String fileDownloadUri = getFileDownloadUri(fileRequest.getUserFilePermissions(), uuidFileName);
            String fileHash = getFileHash(multipartToFile(fileRequest.getFile()));
            MultipartFile file = fileRequest.getFile();

            MyFiles myFile = MyFiles.builder()
                    .fileDownloadPath(fileDownloadUri)
                    .fileContentType(file.getContentType())
                    .fileHashCode(fileHash)
                    .fileOrgName(file.getOriginalFilename())
                    .filePath(savePath)
                    .fileSize(file.getSize())
                    .fileStatus(FileStatus.Registered)
                    .userFilePermissions(addDefaultPermission())
                    .filePermissionGroups(addUserAccessCode(fileRequest.getIdAccessCodes()))
                    .filePhyName(uuidFileName)
                    .fileOwnerByUserCode(ownerCheckSum(fileRequest.getOwnerDomainCode(), fileRequest.getOwnerAuthenticationCode()))
                    .postLinkType("")
                    .postLinked(0L)
                    .build();

            myFilesRopository.save(myFile);

            if (fileRequest.getThumbnailWiths() != null && fileRequest.getThumbnailWiths().stream().count() > 0) {
                saveThumbnailImage(myFile, fileRequest.getFile().getInputStream(), fileRequest.getThumbnailWiths());
            }

            return FileMetadataResponse.builder().myFiles(myFile).build();

        }catch(IOException e){
            e.printStackTrace();
        }
        return new FileMetadataResponse();
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
                infoRequest.getFilePhyName(), infoRequest.getFileCheckSum())
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


    private FileOwner ownerCheckSum(String ownerDomainCode, String ownerAuthenticationCode){

        FileOwner fileOwner = ownerInformationConfirmation(ownerDomainCode, ownerAuthenticationCode );

        if(fileOwner == null){
            fileOwner = fileOwnerRepository.save(FileOwner.builder()
                    .ownerDomainCheckSum(makeMD5StringToChecksum(ownerDomainCode))
                    .ownerAuthenticationCode(makeMD5StringToChecksum(ownerAuthenticationCode))
                    .build()
            );
        }

        return fileOwner;
    }

    private FileOwner ownerInformationConfirmation(String ownerDomainCode, String ownerAuthenticationCode){
        FileOwner fileOwner =  fileOwnerRepository
                .findByOwnerDomainCheckSumAndOwnerAuthenticationCheckSum(
                        makeMD5StringToChecksum(ownerDomainCode),
                        makeMD5StringToChecksum(ownerAuthenticationCode)).orElse(null);
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
     * @return
     */
    private List<String> addDefaultPermission() {
            List<String> filePermissions = new ArrayList<>();
            filePermissions.add(OWNER_WRITE.getPermission());
            filePermissions.add(OWNER_READ.getPermission());

        return filePermissions;
    }


    /**
     * 업로드된 파일을 지정된 경로에 저장한다.
     * @param userFilePermissions
     * @return 저장된 경로를 반환한다.
     */
    private String storeFile(InputStream fileInputStream,
                             List<String> userFilePermissions,
                             String uuidFileName,
                             String subPath) {
        try {

            String rootPath =
                    isPublicPermission(userFilePermissions)?
                            fileStorageProperties.getPublicSpaceDir():
                            fileStorageProperties.getUploadDir();

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
            Files.copy(fileInputStream, savePath, StandardCopyOption.REPLACE_EXISTING);

            return savePath.toString();

        } catch (IOException ex) {
            throw new FileStorageException("Could not store file. Please try again!");
        }
    }

    /**
     * 업로드 파일이 이미지 파일경우 리사이즈 버전을 만든다.
     */
    private File resizeImage(File rootImage, File outImage, int targetWidth, int targetHeight, String imageFormat) throws Exception {
        BufferedImage originalImage = ImageIO.read(rootImage); //todo 세로 가로 구분 하지 못하는 것 보정 필요.

        if( originalImage.getWidth() > 5000){
            throw new OverImagePixelException("3840 pixel over");
        }

        if( originalImage.getHeight() > 5000){
            throw new OverImagePixelException("3840 pixel over");
        }

        double widthRatio  = (double)targetWidth / (double)originalImage.getWidth();
        int imageHeight = targetHeight > 0 ? targetHeight : (int)(originalImage.getHeight() * widthRatio);

        log.info("originalImage.getType() >> "+String.valueOf(originalImage.getType()));
        log.info("originalImage.getWidth() >> "+String.valueOf(originalImage.getWidth()));
        log.info("originalImage.getHeight() >> "+ String.valueOf(originalImage.getHeight()));
        log.info("widthRatio >> "+ widthRatio);
        log.info("targetWidth >> "+ targetWidth + " imageHeight >> "+imageHeight);

        Thumbnails.of(rootImage)
                .size(targetWidth, imageHeight)
                .outputFormat(imageFormat)
                .outputQuality(1)
                .toFile(outImage);

        return outImage;
    }

    /**
     * 이미지 파일정보에서 메다 정보를 추출하여 회신 한다.
     * @param rootImage
     * @return
     * @throws ImageProcessingException
     * @throws IOException
     * @throws MetadataException
     */
    private Metadata getImageMetaInfo(File rootImage) throws ImageProcessingException, IOException, MetadataException {
        Metadata metadata = ImageMetadataReader.readMetadata(rootImage);
        Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
        int orientation = 1; // 회전정보, 1. 0도, 3. 180도, 6. 270도, 8. 90도 회전한 정보
        double deggre = 0D;

        if(directory != null){
            orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION); // 회전정보
        }

        switch (orientation) {
            case 6: deggre = 90D; break;
            case 3: deggre = 180D;break;
            case 8: deggre = 270D;break;
            case 1: break;
            default: orientation=1;break;
        }

        return metadata;
    }

    private void getImageType(File rootImage) throws IOException {
        BufferedImage originalImage = ImageIO.read(rootImage);

        /**
         * jpg, jpeg, png 는 아래와 같은 이미지 특징을 가진단다. (인터넷)
         */
        if (originalImage.getType() == BufferedImage.TYPE_INT_ARGB
                || originalImage.getType() == BufferedImage.TYPE_INT_ARGB_PRE
                || originalImage.getType() == BufferedImage.TYPE_3BYTE_BGR
                || originalImage.getType() == BufferedImage.TYPE_BYTE_GRAY
                || originalImage.getType() == BufferedImage.TYPE_BYTE_INDEXED) {

            log.info("originalImage.getType() is "+originalImage.getType());
        }
    }

    /**
     * 요청에 public 인자가 있는지 점검한다.
     * @param userFilePermissions
     * @return public 인지 아닌지 리턴
     */
    private boolean isPublicPermission(List<String> userFilePermissions){
        final boolean[] result = {false};
        userFilePermissions.forEach(filePermission->{
            if(filePermission.equals(PUBLIC_READ.getPermission())){
                result[0] = true;
            }
        });

        return result[0];
    }

    /**
     * download path를 정한다.
     * @param userPermission
     * @param fullPath
     * @return
     */
    private String getFileDownloadUri(List<String> userPermission, String fullPath){
        String downloadPath = isPublicPermission(userPermission)?
                this.fileStorageProperties.getDownloadPublicPath().concat(fullPath):
                this.fileStorageProperties.getDownloadPath().concat(fullPath);

        return downloadPath;
    }

    /**
     * multipart to File
     * @param multipart
     * @return
     * @throws IllegalStateException
     * @throws IOException
     */
    private File multipartToFile(MultipartFile multipart) throws IllegalStateException, IOException
    {
        File convFile = new File( multipart.getOriginalFilename());
        multipart.transferTo(convFile);

        return convFile;
    }


    private MultipartFile fileToMultipartFile(File file) throws IOException {
        FileItem fileItem = new DiskFileItem(
                "originFile",
                Files.probeContentType(file.toPath()),
                false, file.getName(), (int) file.length(), file.getParentFile());

        try {
            InputStream input = new FileInputStream(file);
            OutputStream os = fileItem.getOutputStream();
            IOUtils.copy(input, os);
            // Or faster..
            // IOUtils.copy(new FileInputStream(file), fileItem.getOutputStream());
        } catch (IOException ex) {
            // do something.
            ex.printStackTrace();
        }

        //jpa.png -> multipart 변환
        MultipartFile mFile = new CommonsMultipartFile(fileItem);
        return mFile;
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
     * @param fileName
     * @return uuid 로 파일명이 변경된 파일명 리턴.
     * @throws IOException
     */
    private String getUUIDFileName(String  fileName) {
        String ext = FilenameUtils.getExtension(
                StringUtils.cleanPath(fileName));

        String uuidFileName = UUID.randomUUID().toString();

        return uuidFileName.concat(".").concat(ext);
    }

    /**
     * 사이즈에 맞추어 _사이즈 형태로 반환한다.
     * @param originalFileName
     * @param imageSize
     * @return
     */
    private String getThumbnailName(String  originalFileName, String imageSize) {
        String ext = FilenameUtils.getExtension(
                StringUtils.cleanPath(originalFileName));

        String thumbnailName = FilenameUtils.getBaseName(
                StringUtils.cleanPath(originalFileName));

        return thumbnailName.concat("_").concat(imageSize).concat(".").concat(ext);
    }

    /**
     * file hash 값 찾기 만든가.
     * @param file
     * @return
     * @throws IOException
     */
    private String getFileHash(File file)  {
        String digestFileName = "";
        try{
            digestFileName = DigestUtils.md5Hex(new FileInputStream(file));
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
