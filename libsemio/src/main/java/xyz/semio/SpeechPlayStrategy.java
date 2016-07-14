package xyz.semio;

import android.app.Activity;

/**
 * Uses a SpeechHelper to emit and recognize speech
 */
public class SpeechPlayStrategy implements PlayStrategy {
  private SpeechHelper _speech;

  public SpeechPlayStrategy(final SpeechHelper speech)
  {
    this._speech = speech;
  }

  @Override
  public Promise<String> recognize() {
    return _speech.recognize();

  }

  @Override
  public Promise<Void> emit(InteractionState state) {

    return _speech.say(state.getUtterance()).then(new Function<SpeechHelper.SpeechStatus, Void>() {
      @Override
      public Void apply(SpeechHelper.SpeechStatus status) {
        return null;
      }
    });
  }
}
