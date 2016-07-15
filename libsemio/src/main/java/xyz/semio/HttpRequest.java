package xyz.semio;

import android.os.AsyncTask;
import android.renderscript.ScriptGroup;
import android.util.JsonWriter;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

public class HttpRequest {
  private URL _url;
  private String _method;
  private JSONObject _data;
  private Map<String, String> _extraHeaders = new HashMap<String, String>();

  HttpRequest(final URL url, final String method) {
    this._url = url;
    this._method = method;
  }

  HttpRequest(final URL url, final String method, final JSONObject data) {
    this._url = url;
    this._method = method.toUpperCase();
    this._data = data;
  }

  public void addHeader(final String key, final String value) {
    this._extraHeaders.put(key, value);
  }

  static String streamToString(InputStream in) throws IOException {
    Scanner s = new Scanner(in).useDelimiter("\\A");
    return s.hasNext() ? s.next() : "";
  }

  private static final int MAX_RETRIES = 5;

  private HttpResponse fetchResult() {
    return fetchResult(0);
  }

  private HttpResponse fetchResult(int reentryCount) {
    HttpsURLConnection conn = null;
    int code = 0;
    JSONObject data = null;
    JSONArray arr = null;
    try {
      conn = (HttpsURLConnection)_url.openConnection();

      conn.setReadTimeout(10000);
      conn.setConnectTimeout(15000);
      conn.setRequestMethod(_method);

      //conn.setRequestProperty("Connection", "close");
      //conn.setRequestProperty("Accept", "application/json");
      for(final String key : _extraHeaders.keySet()) {
        conn.setRequestProperty(key, _extraHeaders.get(key));
      }

      String toWrite = _data != null ? _data.toString() : null;
      boolean outputting = !_method.equalsIgnoreCase("GET") && toWrite != null;

      conn.setDoOutput(outputting);
      conn.setDoInput(true);

      if(outputting) {
        conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
        conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
        conn.setFixedLengthStreamingMode(toWrite.getBytes().length);
      }

      //conn.connect();

      if(outputting) {
        OutputStream out = conn.getOutputStream();
        out.write(toWrite.getBytes());
        out.flush();
      }

      code = conn.getResponseCode();

      InputStream in = conn.getInputStream();

      final String dataString = streamToString(in);
      System.out.println("RET " + dataString);
      if(dataString.length() > 0 && !dataString.equalsIgnoreCase("OK")) {
        try {
          data = new JSONObject(dataString);
        } catch(final JSONException e) {
          try {
            arr = new JSONArray(dataString);
          } catch(final JSONException e1) {

          }
        }

      }
    } catch (final EOFException e) {
      if(conn != null) conn.disconnect();
      Log.e(Util.TAG, e.toString());
      e.printStackTrace();
      return null;
    } catch(final IOException e) {
      Log.e(Util.TAG, e.toString() + " (code: " + code + ")");
      e.printStackTrace();
      return null;
    } finally {
      if(conn != null) conn.disconnect();
    }

    return new HttpResponse(code, data, arr);
  }

  Promise<HttpResponse> execute() {
    AsyncTask<Promise<HttpResponse>, Void, Void> task = new AsyncTask<Promise<HttpResponse>, Void, Void>() {
      @Override
      protected Void doInBackground(Promise<HttpResponse>... promises) {
        Promise<HttpResponse> ret = promises[0];
        ret.complete(fetchResult());
        return null;
      }
    };

    Promise<HttpResponse> ret = new Promise<HttpResponse>();
    task.execute(ret);
    return ret;
  }
}
