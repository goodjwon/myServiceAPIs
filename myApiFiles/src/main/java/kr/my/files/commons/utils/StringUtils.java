package kr.my.files.commons.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

public class StringUtils {
    public static String stringToChecksum(String arg)  {
        return org.apache.commons.codec.digest.DigestUtils.md5Hex( arg );
    }

    public static <T> Stream<T> collectionToStream(Collection<T> collection) {
        return Optional
                .ofNullable(collection)
                .map(Collection::stream)
                .orElseGet(Stream::empty);
    }
}
