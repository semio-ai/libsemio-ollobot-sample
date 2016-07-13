package xyz.semio;

public class InteractionState {
  private String _utterance;
  private int _nextNode;
  private String _script;

  InteractionState(final String utterance, final String script, final int next) {
    this._utterance = utterance;
    this._script = script;
    this._nextNode = next;
  }

  public int getNextNode() {
    return this._nextNode;
  }
  public String getUtterance() {
    return this._utterance;
  }
  public String getScript() {
    return this._script;
  }
}
