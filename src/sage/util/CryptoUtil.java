package sage.util;

import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Anurag Gautam on 03-03-2016.
 */
public class CryptoUtil {
    private CryptoUtil() {
    }

    public static String hash(String algorithm, String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        return DatatypeConverter.printHexBinary(digest.digest(text.getBytes("UTF-8")));
    }

    public static String sha1(String text) {
        try {
            return hash("SHA-1", text);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String sha256(String text) {
        try {
            return hash("SHA-256", text);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String md5(String text) {
        try {
            return hash("MD5", text);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
