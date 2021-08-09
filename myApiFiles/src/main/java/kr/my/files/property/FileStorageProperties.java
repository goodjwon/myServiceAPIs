package kr.my.files.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "file")
public class FileStorageProperties {
    private String uploadDir;
    private String publicSpaceDir;
    private String protectSpaceDir;
    private String downloadPath;
    private String downloadPublicPath;

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public String getPublicSpaceDir() {
        return publicSpaceDir;
    }

    public void setPublicSpaceDir(String publicSpaceDir) {
        this.publicSpaceDir = publicSpaceDir;
    }

    public String getProtectSpaceDir() {
        return protectSpaceDir;
    }

    public void setProtectSpaceDir(String protectSpaceDir) {
        this.protectSpaceDir = protectSpaceDir;
    }

    public String getDownloadPath() {
        return downloadPath;
    }

    public void setDownloadPath(String downloadPath) {
        this.downloadPath = downloadPath;
    }

    public String getDownloadPublicPath() {
        return downloadPublicPath;
    }

    public void setDownloadPublicPath(String downloadPublicPath) {
        this.downloadPublicPath = downloadPublicPath;
    }
}