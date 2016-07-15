package xyz.semio;


import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Session {
  private String _username;

  public Session(final String username) {
    this._username = username;
  }

  /**
   * Creates a new stateful Interaction based on a given Graph ID.
   *
   * @param graphId The 36 character unique identifier of the dialog graph
   * @return A deferred Interaction instance or null if the interaction couldn't be created
   */
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

  public Promise<List<GraphInfo>> getGraphs() {
    System.out.println("Getting graphs");
    HttpRequest req = new HttpRequest(Util.makeApiUrl("account/" + _username + "/graphs"), "GET");
    return req.execute().then(new Function<HttpResponse, List<GraphInfo>>() {
      @Override
      public List<GraphInfo> apply(HttpResponse res) {
        System.out.println("get " + res);
        if(res == null) return null;

        System.out.println(res.getArray().toString());

        JSONArray graphs = res.getArray();

        List<GraphInfo> ret = new ArrayList<GraphInfo>();
        for(int i = 0; i < graphs.length(); ++i) {
          try {
            JSONObject graph = graphs.getJSONObject(i);
            ret.add(new GraphInfo(graph.getString("id"), graph.getString("owner"), graph.getString("name")));
          } catch(final JSONException e) {}
        }
        return ret;
      }
    });
  }
}
