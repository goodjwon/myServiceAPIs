package kr.my.files.dto;

import kr.my.files.entity.FilePermissionGroup;
import kr.my.files.entity.MyFiles;
import kr.my.files.enums.UserFilePermissions;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
public class UploadFileMetadataResponse extends RepresentationModel<UploadFileMetadataResponse> implements Serializable {
    private String ownerDomainCode;
    private String ownerAuthenticationCode;
    private String fileName;
    private String fileDownloadUri;
    private String fileType;
    private String originFileName;
    private String checkSum;
    private long size;
    private List<String> filePermissions;
    private List<FilePermissionGroup> filePermissionGroups;

    @Builder
    public  UploadFileMetadataResponse(MyFiles myFiles) {
        this.fileName = myFiles.getFilePhyName();
        this.fileDownloadUri = myFiles.getFileDownloadPath();
        this.fileType = myFiles.getFileContentType();
        this.originFileName = myFiles.getFileOrgName();
        this.size = myFiles.getFileSize();
        this.checkSum = myFiles.getFileHashCode();
        this.filePermissions = myFiles.getUserFilePermissions();
        this.filePermissionGroups = myFiles.getFilePermissionGroups();
        this.ownerAuthenticationCode = myFiles.getFileOwnerByUserCode().getOwnerAuthenticationCheckSum();
        this.ownerDomainCode = myFiles.getFileOwnerByUserCode().getOwnerDomainCheckSum();
    }


    public void addFilePermission(List<String> filePermissions){
        this.filePermissions = filePermissions;
    }

}
