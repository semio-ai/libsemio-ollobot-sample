package xyz.semio;

import android.util.Log;

import xyz.semio.ollobot.Ollobot;
import xyz.semio.script.Debug;
import xyz.semio.script.ScriptInterpreter;
import xyz.semio.script.Time;

/**
 * This convenience class enables asynchronous playback of Interactions with a given PlayStrategy
 */
public class InteractionPlayer {
  private Interaction _interaction;
  private PlayStrategy _strategy;
  private ScriptInterpreter _scriptInterpreter = new ScriptInterpreter();

  private boolean _playing = false;

  /**
   * Creates a new InteractionPlayer.
   *
   * @param interaction The Interaction to play
   * @param strategy The method for emitting and recognizing input
   */
  public InteractionPlayer(final Interaction interaction, final PlayStrategy strategy) {
    if(interaction == null) throw new NullPointerException("interaction can't be null.");
    if(strategy == null) throw new NullPointerException("strategy can't be null.");
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

      Promise<Void> em = _strategy.emit(value);
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
      em.then(new Function<Void, Object>() {
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

  /**
   * Starts the interaction
   */
  public void start() {
    if(this._playing) return;
    this._playing = true;
    this._recognize();
  }

  /**
   * Schedules the Interaction to be stopped
   */
  public void stop() {
    if(!this._playing) return;
    this._playing = false;
  }
}
