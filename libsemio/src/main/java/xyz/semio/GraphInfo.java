package xyz.semio;

import java.util.Date;

public class GraphInfo {
  private String _id;
  private String _owner;
  private String _name;

  public GraphInfo(final String id, final String owner, final String name) {
    this._id = id;
    this._owner = owner;
    this._name = name;
  }

  public String getId() {
    return this._id;
  }

  public String getOwner() {
    return this._owner;
  }

  public String getName() {
    return this._name;
  }
}
