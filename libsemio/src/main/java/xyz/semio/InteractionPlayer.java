package xyz.semio;

import android.util.Log;

import xyz.semio.ollobot.Ollobot;
import xyz.semio.script.Debug;
import xyz.semio.script.ScriptInterpreter;
import xyz.semio.script.Time;

public class InteractionPlayer {
  private Interaction _interaction;
  private PlayStrategy _strategy;
  private ScriptInterpreter _scriptInterpreter = new ScriptInterpreter();

  private boolean _playing = false;

  public InteractionPlayer(final Interaction interaction, final PlayStrategy strategy) {
    this._interaction = interaction;
    this._strategy = strategy;
    this._scriptInterpreter.addBinding(new Debug(), "debug");
    this._scriptInterpreter.addBinding(new Time(), "time");
  }

  public Interaction getInteraction() {
    return this._interaction;
  }
  public PlayStrategy getStrategy() {
    return this._strategy;
  }
  public ScriptInterpreter getScriptInterpreter() {
    return this._scriptInterpreter;
  }

  private Function<InteractionState, Object> _emitPhase = new Function<InteractionState, Object>() {
    @Override
    public Object apply(InteractionState value) {
      if(!_playing) return null;

      final String script = value.getScript();

      Thread t = new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            _scriptInterpreter.execute(script);
          } catch(final Throwable t) {
            t.printStackTrace();
          }
        }
      });
      t.run();
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
