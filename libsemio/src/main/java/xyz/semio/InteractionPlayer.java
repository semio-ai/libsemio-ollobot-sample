package xyz.semio;

import android.util.Log;

import xyz.semio.script.Android;
import xyz.semio.script.ScriptInterpreter;

public class InteractionPlayer {
  private Interaction _interaction;
  private PlayStrategy _strategy;
  private ScriptInterpreter _scriptInterpreter = new ScriptInterpreter();

  private boolean _playing = false;

  public InteractionPlayer(final Interaction interaction, final PlayStrategy strategy) {
    this._interaction = interaction;
    this._strategy = strategy;
    this._scriptInterpreter.addBinding(new Android(), "android");
  }

  public Interaction getInteraction() {
    return this._interaction;
  }
  public PlayStrategy getStrategy() {
    return this._strategy;
  }
  public ScriptInterpreter scriptInterpreter() {
    return this._scriptInterpreter;
  }

  private Function<InteractionState, Object> _emitPhase = new Function<InteractionState, Object>() {
    @Override
    public Object apply(InteractionState value) {
      if(!_playing) return null;
      try {
        _scriptInterpreter.execute(value.getScript());
      } catch(Throwable t) {
        Log.e("script execution", t.getMessage());
      }
      _strategy.emit(value).then(new Function<Void, Object>() {
        @Override
        public Object apply(Void value) {
          if(!_playing) return null;
          _recognize();
          return null;
        }
      });
      return null;
    }
  };

  private Function<String, Object> _recPhase = new Function<String, Object>() {
    @Override
    public Object apply(String value) {
      if(!_playing) return null;
      _interaction.next(value).then(_emitPhase);
      return null;
    }
  };

  private void _recognize() {
    if(!this._playing) return;
    _strategy.recognize().then(_recPhase);
  }

  public void start() {
    if(this._playing) return;
    this._playing = true;
    this._recognize();
  }

  public void stop() {
    if(!this._playing) return;
    this._playing = false;
  }
}
