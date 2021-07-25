package kr.my.files.commons.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class StringUtils {
    public static String stringToChecksum(String arg)  {
        return org.apache.commons.codec.digest.DigestUtils.md5Hex( arg );
    }
}
