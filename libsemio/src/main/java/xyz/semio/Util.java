package xyz.semio;

import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class Util {
  public static String API_BASE = "http://10.0.1.4/v1";
  public static String TAG = "libsemio";

  public static URL makeApiUrl(final String extension) {
    URL ret = null;
    try {
      ret = new URL(API_BASE + "/" + extension);
    } catch(final MalformedURLException e) {
      Log.e(TAG, "Malformed URL: " + e.getMessage());
      return null;
    }
    return ret;
  }

  private final static char[] hexArray = "0123456789abcdef".toCharArray();
  public static String bytesToHex(byte[] bytes) {
    char[] hexChars = new char[bytes.length * 2];
    for ( int j = 0; j < bytes.length; j++ ) {
      int v = bytes[j] & 0xFF;
      hexChars[j * 2] = hexArray[v >>> 4];
      hexChars[j * 2 + 1] = hexArray[v & 0x0F];
    }
    return new String(hexChars);
  }

  public static String shaHex(final String in) {
    MessageDigest sha = null;
    try {
      sha = MessageDigest.getInstance("SHA-256");
    } catch(final NoSuchAlgorithmException e) {
      return null;
    }

    sha.update(in.getBytes());
    return bytesToHex(sha.digest());
  }
}
