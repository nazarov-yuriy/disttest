package org.strangeway.disttest

import java.security.MessageDigest


class Utils {
    static String calcHash(byte[] bytes) {
        return new BigInteger(
                1,
                MessageDigest
                        .getInstance("SHA1")
                        .digest(bytes)
        ).toString(16);
    }

    static String calcHash(String str){
        calcHash(str.getBytes())
    }
}
