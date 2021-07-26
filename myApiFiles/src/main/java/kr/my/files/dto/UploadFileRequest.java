package kr.my.files.dto;


import kr.my.files.enums.UserFilePermissions;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
public class UploadFileRequest {
    @NotEmpty
    private String fileName;
    @NotEmpty
    private MultipartFile file;
    private MultipartFile[] files;
    @NotEmpty
    private String ownerDomainCode;
    @NotEmpty
    private String ownerAuthenticationCode;

    @NotEmpty
    private List <UserFilePermissions> userFilePermissions;
    @NotEmpty
    private List<String> idAccessCodes;

    public void addFile(MultipartFile file){
        this.file = file;
    }

    public void addFiles(MultipartFile[] files){
        this.files = files;
    }

    public void addUserFilePermissions(List <UserFilePermissions> userFilePermissions){
        this.userFilePermissions = userFilePermissions;
    }

    public void addFileName(String fileName){
        this.fileName = fileName;
    }

}
