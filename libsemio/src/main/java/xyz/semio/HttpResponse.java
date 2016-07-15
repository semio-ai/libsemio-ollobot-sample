package xyz.semio;

import org.json.JSONArray;
import org.json.JSONObject;

public class HttpResponse {
  private JSONObject _data;
  private JSONArray _array;
  private int _code;

  HttpResponse(final int code) {
    this._code = code;
  }

  HttpResponse(final int code, final JSONObject data, final JSONArray array) {
    this._code = code;
    this._data = data;
    this._array = array;
  }

  public int getCode() {
    return this._code;
  }

  public JSONObject getData() {
    return this._data;
  }
  public JSONArray getArray() { return this._array; }

  @Override
  public String toString() {
    return this._code + (this._data != null ? " " + this._data.toString() : "");
  }
}
