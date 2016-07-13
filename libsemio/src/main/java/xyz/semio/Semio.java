package xyz.semio;

import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.PasswordAuthentication;

public class Semio {
  public static Promise<Session> createSession(final String username, final String password) throws SessionException {
    // FIXME: Shouldn't be the global one
    Authenticator.setDefault(new Authenticator() {
      @Override
      protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(username, Util.shaHex(password).toCharArray());
      }
    });

    CookieManager cookieManager = new CookieManager();
    CookieHandler.setDefault(cookieManager);

    HttpRequest req = new HttpRequest(Util.makeApiUrl("login"), "POST");

    return req.execute().then(new Function<HttpResponse, Session>() {
      @Override
      public Session apply(HttpResponse value) {
        if(value == null || value.getCode() != 200) return null;
        return new Session();
      }
    });
  }
}
