package kr.my.files.commons.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class StringUtils {
    public static String textToHash(String arg)  {
        MessageDigest md;
        StringBuffer result = null;

        try {
            md = MessageDigest.getInstance("SHA-256");
            result = new StringBuffer();
            byte[] hashData = md.digest(arg.getBytes(StandardCharsets.UTF_8));

            for(byte b: hashData)
                result.append(Integer.toString((b &0xff) +0x100,16).substring(1));

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return result.toString();
    }
}
