package kr.my.files.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.my.files.entity.FilePermissionGroup;
import kr.my.files.entity.MyFiles;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@Getter
@EqualsAndHashCode(callSuper = false)
@Relation(collectionRelation = "files", itemRelation = "file")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileMetadataResponse extends RepresentationModel<FileMetadataResponse> implements Serializable {
    private String ownerDomainCode;
    private String ownerAuthenticationCode;
    private String fileName;
    private String fileDownloadUri;
    private String fileType;
    private String originFileName;
    private String checkSum;
    private long size;
    private List<String> filePermissions = new ArrayList<>();;
    private List<String> thumbnailImagePaths = new ArrayList<>();;
    private List<String> filePermissionGroups = new ArrayList<>();

    @Builder
    public FileMetadataResponse(MyFiles myFiles) {
        this.fileName = myFiles.getFilePhyName();
        this.fileDownloadUri = myFiles.getFileDownloadPath();
        this.fileType = myFiles.getFileContentType();
        this.originFileName = myFiles.getFileOrgName();
        this.size = myFiles.getFileSize();
        this.checkSum = myFiles.getFileHashCode();
        this.filePermissions = myFiles.getUserFilePermissions();
        this.ownerAuthenticationCode = myFiles.getFileOwnerByUserCode().getOwnerAuthenticationCheckSum();
        this.ownerDomainCode = myFiles.getFileOwnerByUserCode().getOwnerDomainCheckSum();
    }

    public void addFileThumbnailImagePaths(List<String> thumbnailImagePaths){
        this.thumbnailImagePaths = thumbnailImagePaths;
    }

    public void addFilePermissionGroup(List<FilePermissionGroup> groups){
        this.filePermissionGroups = groups.stream().map(group->group.getIdAccessCode()).collect(Collectors.toList());
    }
}
