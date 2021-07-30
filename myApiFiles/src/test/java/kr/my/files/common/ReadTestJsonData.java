package kr.my.files.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;

public class ReadTestJsonData {
    public static <T> T readValue(String filePath, Class<T> valueType) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        File resource = new ClassPathResource(filePath).getFile();

        return  mapper.readValue(resource, valueType);
    }
}
