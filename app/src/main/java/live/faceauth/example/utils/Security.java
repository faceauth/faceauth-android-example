package live.faceauth.example.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Security {

  private static String convertToHex(byte[] data) {
    StringBuilder buf = new StringBuilder();
    for (byte b : data) {
      int halfbyte = (b >>> 4) & 0x0F;
      int two_halfs = 0;
      do {
        buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte)
            : (char) ('a' + (halfbyte - 10)));
        halfbyte = b & 0x0F;
      } while (two_halfs++ < 1);
    }
    return buf.toString();
  }

  public static String SHA1(String text) {
    String sha = null;
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-1");
      byte[] textBytes = new byte[0];
      textBytes = text.getBytes("iso-8859-1");
      md.update(textBytes, 0, textBytes.length);
      byte[] sha1hash = md.digest();

      sha = convertToHex(sha1hash);
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return sha;
  }
}