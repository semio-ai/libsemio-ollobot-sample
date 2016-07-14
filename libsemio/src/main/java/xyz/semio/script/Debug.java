package xyz.semio.script;

import android.util.Log;

public class Debug {
  private static final String TAG = "script";

  public void log(String message) {
    Log.i(TAG, message);
  }
}
