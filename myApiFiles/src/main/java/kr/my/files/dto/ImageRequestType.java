package kr.my.files.dto;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImageRequestType {

    private int maxWith;
    private int maxHeight;
    private int thumbnailWith;
    private int thumbnailHeight;

}
