package kr.my.files.dto;

import lombok.*;

@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Data
public class FileSaveResult {
    private String fileDownloadUri;
    private String fileContentType;
    private String fileHashCode;
    private String fileOrgName;
    private String fileSavePath;
    private Long fileSize;
    private String filePhyName;
}
