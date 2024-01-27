package org.example;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * 一次性口令
 *
 * @author snow-zen
 */
public class TotpTokenGenerator {

    private static byte[] hexStrToByte(String hexStr) {
        // 添加一个字节使得 0 开头的十六进制字符得以转换
        byte[] bArray = new BigInteger("10" + hexStr, 16).toByteArray();
        return Arrays.copyOfRange(bArray, 1, bArray.length);
    }

    private static String fillLength(String str, Integer length) {
        if (str.length() > length) {
            return str;
        }

        StringBuilder result = new StringBuilder(str);
        for (int i = str.length(); i < length; i++) {
            result.insert(0, "0");
        }
        return result.toString();
    }

    private static byte[] hashOfHmac(String key, String message) {
        try {
            byte[] keyBytes = hexStrToByte(key);
            byte[] messageBytes = hexStrToByte(message);

            Mac mac = Mac.getInstance("HMacSHA1");
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "RAWS");
            mac.init(keySpec);
            return mac.doFinal(messageBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 自 Unix 纪元以秒为单位指定的纪元（如果使用 Unix 时间戳，默认为 0）
     */
    private final Long t0 = 0L;

    /**
     * 时间间隔步长（默认为 30 秒）
     */
    private final Long tx = 30L;

    public String generateTotpToken(String key, Long timestamp, Integer length) {
        // 补充到固定的长度 16，得到一个固定 64 位的十六进制值，方便与验证方进行比较和验证
        String ctHexStr = fillLength(Long.toHexString((timestamp - t0) / tx), 16);

        byte[] hash = hashOfHmac(key, ctHexStr);

        int offset = hash[hash.length - 1] & 0xf;
        int totp = ((hash[offset] & 0x7f) << 24)
                | ((hash[offset + 1] & 0xff) << 16)
                | ((hash[offset + 2] & 0xff) << 8)
                | (hash[offset + 3] & 0xff);

        int result = (int) (totp % Math.pow(10, length));
        return fillLength(String.valueOf(result), length);
    }
}
