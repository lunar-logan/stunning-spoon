package sage.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Anurag Gautam on 03-03-2016.
 */
public class CryptoUtil {
    private CryptoUtil() {
    }

    public static String hash(String algorithm, String text) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        return new String(digest.digest(text.getBytes()));
    }

    public static String sha1(String text) throws NoSuchAlgorithmException {
        return hash("SHA-1", text);
    }

    public static String sha256(String text) throws NoSuchAlgorithmException {
        return hash("SHA-256", text);
    }

    public static String md5(String text) throws NoSuchAlgorithmException {
        return hash("MD5", text);
    }
}
