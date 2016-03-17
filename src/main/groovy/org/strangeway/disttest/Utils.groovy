package org.strangeway.disttest

import java.security.MessageDigest


class Utils {
    private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

    static String calcHash(byte[] bytes) {
        byte[] hash = MessageDigest
                .getInstance("SHA1")
                .digest(bytes)

        char[] chars = new char[hash.length * 2];
        for (int i = 0; i < hash.length; i++) {
            chars[i * 2] = HEX_DIGITS[(hash[i] >> 4) & 0xf];
            chars[i * 2 + 1] = HEX_DIGITS[hash[i] & 0xf];
        }
        return new String(chars);
    }

    static String calcHash(String str){
        calcHash(str.getBytes())
    }
}
