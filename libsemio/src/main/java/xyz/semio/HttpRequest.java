package xyz.semio;

import android.os.AsyncTask;
import android.renderscript.ScriptGroup;
import android.util.JsonWriter;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;

import java.net.HttpURLConnection;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

public class HttpRequest {
  private URL _url;
  private String _method;
  private JSONObject _data;

  HttpRequest(final URL url, final String method) {
    this._url = url;
    this._method = method;
  }

  HttpRequest(final URL url, final String method, final JSONObject data) {
    this._url = url;
    this._method = method.toUpperCase();
    this._data = data;
  }

  static String streamToString(InputStream in) {
    Scanner s = new Scanner(in).useDelimiter("\\A");
    return s.hasNext() ? s.next() : "";
  }

  Promise<HttpResponse> execute() {
    AsyncTask<Promise<HttpResponse>, Void, Void> task = new AsyncTask<Promise<HttpResponse>, Void, Void>() {
      @Override
      protected Void doInBackground(Promise<HttpResponse>... promises) {
        Promise<HttpResponse> ret = promises[0];

        HttpURLConnection conn = null;
        int code = 0;
        try {
          conn = (HttpURLConnection)_url.openConnection();
          conn.setReadTimeout(1000);
          conn.setConnectTimeout(3000);
          conn.setRequestMethod(_method);

          String toWrite = _data != null ? _data.toString() : null;
          boolean outputting = !_method.equalsIgnoreCase("GET") && toWrite != null;

          conn.setDoOutput(outputting);
          conn.setDoInput(true);

          if(outputting) {
            conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
            conn.setFixedLengthStreamingMode(toWrite.getBytes().length);
          }

          conn.connect();

          if(outputting) {
            OutputStream out = conn.getOutputStream();
            out.write(toWrite.getBytes());
            out.flush();
          }

          code = conn.getResponseCode();
          InputStream in = conn.getInputStream();

          final String dataString = streamToString(in);
          JSONObject data = null;
          if(dataString.length() > 0 && !dataString.equalsIgnoreCase("OK")) {
            data = new JSONObject(dataString);
          }
          ret.complete(new HttpResponse(code, data));

        } catch(final IOException e) {
          Log.e(Util.TAG, e.toString() + " (code: " + code + ")");
          ret.complete(null);
        } catch(final JSONException e) {
          Log.e(Util.TAG, e.getMessage());
          ret.complete(null);
        } finally {
          if(conn != null) conn.disconnect();
        }

        return null;
      }
    };

    Promise<HttpResponse> ret = new Promise<HttpResponse>();
    task.execute(ret);
    return ret;
  }
}
