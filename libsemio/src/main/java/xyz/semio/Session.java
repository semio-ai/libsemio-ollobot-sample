package xyz.semio;


import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class Session {
  public Promise<Interaction> createInteraction(final String graphId) {
    Promise<Interaction> ret = new Promise<Interaction>();
    JSONObject data = new JSONObject();
    try {
      data.put("graph_id", graphId);
    } catch(final JSONException e) {
      Log.e(Util.TAG, e.getMessage());
      ret.complete(null);
      return ret;
    }
    HttpRequest req = new HttpRequest(Util.makeApiUrl("interaction"), "POST", data);
    return req.execute().then(new Function<HttpResponse, Interaction>() {
      @Override
      public Interaction apply(HttpResponse value) {
        if(value == null) return null;
        JSONObject res = value.getData();
        String id = null;
        try {
          id = res.getString("id");
        } catch(final JSONException e) {
          Log.e(Util.TAG, e.getMessage());
          return null;
        }
        return new Interaction(id);
      }
    });

  }
}
