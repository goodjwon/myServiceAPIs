package kr.my.files.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class FileSaveResult {
    private String fileDownloadUri;
    private String fileContentType;
    private String fileHashCode;
    private String fileOrgName;
    private String fileSavePath;
    private Long fileSize;
    private String filePhyName;

}
