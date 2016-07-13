package xyz.semio;

import org.json.JSONObject;

public class HttpResponse {
  private JSONObject _data;
  private int _code;

  HttpResponse(final int code) {
    this._code = code;
  }

  HttpResponse(final int code, final JSONObject data) {
    this._code = code;
    this._data = data;
  }

  public int getCode() {
    return this._code;
  }

  public JSONObject getData() {
    return this._data;
  }
}
