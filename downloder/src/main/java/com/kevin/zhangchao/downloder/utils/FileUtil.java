package com.kevin.zhangchao.downloder.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by ZhangChao on 2017/5/14.
 */

public class FileUtil {
    private static final String HASH_ALGORITHM = "MD5";
    private static final int RADIX = 10 + 26; // 10 digits + 26 letters

    public static String getMd5FileName(String url) {
        byte[] md5 = getMD5(url.getBytes());
        BigInteger bi = new BigInteger(md5).abs();
        return bi.toString(RADIX) + url.substring(url.lastIndexOf("/") + 1);
    }

    private static byte[] getMD5(byte[] data) {
        byte[] hash = null;
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            digest.update(data);
            hash = digest.digest();
        } catch (NoSuchAlgorithmException e) {
            Trace.e(e.getMessage());
        }
        return hash;
    }
}
