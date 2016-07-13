package xyz.semio;

import android.app.Activity;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SpeechHelper implements TextToSpeech.OnInitListener {
  private static final int RESULT_SPEECH = 1337;
  private static final String SPEECH_PROMISE = "SPEECH_PROMISE";

  private TextToSpeech _tts = null;

  private int _speechPromiseIter = 1;
  private Map<Integer, Promise<String>> _recPromiseMap = new HashMap<Integer, Promise<String>>();
  private Map<String, Promise<SpeechStatus>> _ttsPromiseMap = new HashMap<String, Promise<SpeechStatus>>();

  private Activity _activity;

  public enum SpeechStatus {
    Done,
    Error
  }

  private class ProgressListener extends UtteranceProgressListener {
    @Override
    public void onStart(String id) {

    }

    @Override
    public void onDone(String id) {
      if(!id.startsWith("SEMIO-")) return;
      _ttsPromiseMap.get(id).complete(SpeechStatus.Done);
      _ttsPromiseMap.remove(id);
    }

    @Override
    public void onError(String id) {
      if(!id.startsWith("SEMIO-")) return;
      _ttsPromiseMap.get(id).complete(SpeechStatus.Error);
      _ttsPromiseMap.remove(id);
    }
  }

  public SpeechHelper(final Activity activity) {
    this._activity = activity;
  }

  private void lazyInit() {
    if(this._tts != null) return;
    this._tts = new TextToSpeech(this._activity, this);
  }

  public Promise<String> recognize() {
    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
    int speechPromiseIt = _speechPromiseIter++;
    Promise<String> ret = new Promise<String>();
    this._recPromiseMap.put(speechPromiseIt, ret);
    intent.putExtra(SPEECH_PROMISE, speechPromiseIt);
    this._activity.startActivityForResult(intent, RESULT_SPEECH);
    return ret;
  }

  public void onInit(int code) {

  }

  public Promise<SpeechStatus> say(final String utterance) {
    this.lazyInit();
    this._tts.setLanguage(Locale.US);
    String id = "SEMIO-" + this._speechPromiseIter++;
    this._tts.speak(utterance, TextToSpeech.QUEUE_ADD, null, id);
    Promise<SpeechStatus> ret = new Promise<SpeechStatus>();
    this._ttsPromiseMap.put(id, ret);
    return ret;
  }

  public void processResult(int requestCode, int resultCode, Intent data) {
    if(requestCode != RESULT_SPEECH) return;
    if (resultCode != Activity.RESULT_OK || data == null) return;

    ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
    int iter = data.getIntExtra(SPEECH_PROMISE, -1);
    if(iter < 0) return;

    this._recPromiseMap.get(iter).complete(text.get(0));
    this._recPromiseMap.remove(iter);
  }
}
