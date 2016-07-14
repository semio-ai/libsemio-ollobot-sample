package xyz.semio;

import android.util.Base64;

import org.json.JSONObject;

import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.PasswordAuthentication;

public class Semio {
  public static Promise<Session> createSession(final String username, final String password) throws SessionException {
    // FIXME: Shouldn't be the global one
    final String auth = Base64.encodeToString((username + ":" + Util.shaHex(password)).getBytes(), Base64.NO_WRAP);
    System.out.println(auth);

    CookieManager cookieManager = new CookieManager();
    CookieHandler.setDefault(cookieManager);

    HttpRequest req = new HttpRequest(Util.makeApiUrl("login"), "POST");
    req.addHeader("Authorization", "Basic " + auth);

    return req.execute().then(new Function<HttpResponse, Session>() {
      @Override
      public Session apply(HttpResponse value) {
        if(value == null || value.getCode() != 204) return null;
        return new Session();
      }
    });
  }
}
