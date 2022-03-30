package kr.my.files.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FilePermissionAddRequest {
    @NotEmpty
    private String filePhyName;
    @NotEmpty
    private String ownerDomainCode;
    @NotEmpty
    private String ownerAuthenticationCode;
    @NotEmpty
    private List<String> additionalIdAccessCode;
    @NotEmpty
    private String fileCheckSum;
}
