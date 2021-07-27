package kr.my.files.dto;

import lombok.*;

import javax.validation.constraints.NotEmpty;

/**
 * 파일다운로드 정보에 대해서 요청한다.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
public class FileInfoRequest {
    @NotEmpty
    private String filePhyName;
    @NotEmpty
    private String ownerDomainCode;
    @NotEmpty
    private String ownerAuthenticationCode;
    @NotEmpty
    private String fileCheckSum;
}
