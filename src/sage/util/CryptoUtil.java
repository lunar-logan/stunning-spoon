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
}
