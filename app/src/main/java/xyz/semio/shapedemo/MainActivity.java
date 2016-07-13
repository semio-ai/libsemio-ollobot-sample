package xyz.semio.shapedemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import xyz.semio.Function;
import xyz.semio.Interaction;
import xyz.semio.InteractionPlayer;
import xyz.semio.InteractionState;
import xyz.semio.PlayStrategy;
import xyz.semio.Promise;
import xyz.semio.Semio;
import xyz.semio.Session;
import xyz.semio.SessionException;
import xyz.semio.SpeechHelper;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
  private ImageButton btnSpeak;
  private TextView txtText;
  private SpeechHelper _speech = new SpeechHelper(this);

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    txtText = (TextView) findViewById(R.id.txtText);
    btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);

    btnSpeak.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        _speech.say("Test");
        /*_speech.recognize().then(new Function<String, Object>() {
          @Override
          public Object apply(String text) {
            txtText.setText(text);
            return null;
          }
        });*/

      }
    });
    Session session = null;
    try {
      Semio.createSession("braden", "testtesttest").then(new Function<Session, Object>() {
        @Override
        public Object apply(Session session) {
          if(session == null) return null;
          System.out.println(session);
          session.createInteraction("93da703a-4cc1-4a47-8701-557c67250587").then(new Function<Interaction, Object>() {
            @Override
            public Object apply(Interaction interaction) {
              System.out.println(interaction);
              InteractionPlayer player = new InteractionPlayer(interaction, new PlayStrategy() {
                @Override
                public Promise<String> recognize() {
                  Promise<String> ret = new Promise<String>();
                  ret.complete("Knock knock");
                  return ret;
                }

                @Override
                public Promise<Void> emit(InteractionState state) {
                  Log.i("semio", state.toString());
                  Promise<Void> ret = new Promise<Void>();
                  // Never finish promise
                  return ret;
                }
              });
              player.start();

              return null;
            }
          });

          return null;
        }
      });
    } catch(final SessionException e) {
      Log.e("semio", e.toString());
    }


  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.activity_main, menu);
    return true;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    this._speech.processResult(requestCode, resultCode, data);
  }
}