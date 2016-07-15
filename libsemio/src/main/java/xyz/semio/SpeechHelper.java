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
  private static final int RESULT_SPEECH = 1300;
  private static final String SPEECH_PROMISE = "SPEECH_PROMISE";

  private TextToSpeech _tts = null;

  private int _speechPromiseIter = 1;
  private int _speechResultIter = 0;
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
      System.out.println(id);
    }

    @Override
    public void onDone(String id) {
      System.out.println(id);
      if(!id.startsWith("SEMIO-")) return;
      _ttsPromiseMap.get(id).complete(SpeechStatus.Done);
      _ttsPromiseMap.remove(id);
    }

    @Override
    public void onError(String id) {
      System.out.println(id);
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
    this._tts.setOnUtteranceProgressListener(new ProgressListener());
  }

  private int nextSpeechResultIter() {
    int next = this._speechResultIter++;
    this._speechResultIter %= 100;
    return next + RESULT_SPEECH;
  }

  public Promise<String> recognize() {
    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
    int speechIt = this.nextSpeechResultIter();
    Promise<String> ret = new Promise<String>();
    this._recPromiseMap.put(speechIt, ret);
    this._activity.startActivityForResult(intent, speechIt);
    return ret;
  }

  public void onInit(int code) {

  }

  public void init() {
    this.lazyInit();
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
    if(requestCode <  RESULT_SPEECH || requestCode >= RESULT_SPEECH + 100) return;
    if (resultCode != Activity.RESULT_OK || data == null) return;

    ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
    System.out.println(text.get(0));
    this._recPromiseMap.get(requestCode).complete(text.get(0));
    this._recPromiseMap.remove(requestCode);
  }
}
