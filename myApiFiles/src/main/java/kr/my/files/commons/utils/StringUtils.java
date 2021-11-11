package kr.my.files.commons.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.commons.codec.digest.DigestUtils;

public class StringUtils {
    public static String stringToChecksum(String arg)  {
        return DigestUtils.md5Hex( arg );
    }

    public static String stringToChecksumSha256(String arg){
        return DigestUtils.sha256Hex(arg);
    }

    public static String fileToChecksum(String filePath) throws IOException {
        File file = new File(filePath);
        byte[] fileContent = Files.readAllBytes(file.toPath());

        return DigestUtils.sha256Hex(fileContent);
    }

    public static <T> Stream<T> collectionToStream(Collection<T> collection) {
        return Optional
                .ofNullable(collection)
                .map(Collection::stream)
                .orElseGet(Stream::empty);
    }
}
