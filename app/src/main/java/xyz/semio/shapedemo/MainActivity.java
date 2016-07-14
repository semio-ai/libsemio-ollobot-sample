package xyz.semio.shapedemo;

import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;

import xyz.semio.Function;
import xyz.semio.Interaction;
import xyz.semio.InteractionPlayer;
import xyz.semio.Semio;
import xyz.semio.Session;
import xyz.semio.SessionException;
import xyz.semio.SpeechHelper;
import xyz.semio.SpeechPlayStrategy;
import xyz.semio.ollobot.Ollobot;
import xyz.semio.ollobot.bluetooth.BluetoothManager;
import xyz.semio.ollobot.service.BTConnectionService;
import xyz.semio.ollobot.utils.Constants;


import android.app.Activity;

import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
  private static final String TAG = "semio";

  private ImageButton btnSpeak;
  private TextView txtText;
  private SpeechHelper _speech = new SpeechHelper(this);

  private Ollobot _ollobot;
  private boolean _serviceBound = false;
  private BTConnectionService _service;
  private ActivityHandler _activityHandler;

  public class ActivityHandler extends Handler {
    @Override
    public void handleMessage(Message msg)
    {
      switch(msg.what) {
        case Constants.MESSAGE_BT_STATE_INITIALIZED:
          break;
        case Constants.MESSAGE_BT_STATE_LISTENING:
          break;
        case Constants.MESSAGE_BT_STATE_CONNECTING:
          break;
        case Constants.MESSAGE_BT_STATE_CONNECTED:
          if(_service != null) {
            String deviceName = _service.getDeviceName();
            if(deviceName != null) {

            }
          }
          break;
        case Constants.MESSAGE_BT_STATE_ERROR:
          break;

        // BT Command status
        case Constants.MESSAGE_CMD_ERROR_NOT_CONNECTED:
          break;

        case Constants.MESSAGE_STATUS_PACKET:
          // We currently don't need to read
        default:
          break;
      }

      super.handleMessage(msg);
    }
  }

  private ServiceConnection _serviceConn = new ServiceConnection() {

    public void onServiceConnected(ComponentName className, IBinder binder) {
      Log.d(TAG, "# Activity - Service connected");

      _service = ((BTConnectionService.ServiceBinder) binder).getService();
      _ollobot = new Ollobot(_service);

      // Activity couldn't work with mService until connections are made
      // So initialize parameters and settings here. Do not initialize while running onCreate()
      initialize();
    }

    public void onServiceDisconnected(ComponentName className) {
      Log.d(TAG, "# Activity - Service disconnected");
      _service = null;
      _ollobot = null;
    }
  };

  private void initialize() {
    _service.setupService(_activityHandler);

    // If BT is not on, request that it be enabled.
    // RetroWatchService.setupBT() will then be called during onActivityResult
    if(_service.isBluetoothEnabled()) return;

    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    startActivityForResult(enableIntent, Constants.REQUEST_ENABLE_BT);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this._activityHandler = new ActivityHandler();

    setContentView(R.layout.activity_main);

    txtText = (TextView) findViewById(R.id.txtText);
    btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);

    startService();

    btnSpeak.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        _speech.say("Test");
        scan();
      }
    });

    ((ImageButton)findViewById(R.id.move)).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startInteraction();
      }
    });

    _speech.say("");
  }

  private void startInteraction() {


    Session session = null;
    try {
      Semio.createSession("", "").then(new Function<Session, Object>() {
        @Override
        public Object apply(Session session) {
          if(session == null) return null;
          System.out.println(session);

          session.createInteraction("e3c9f4b2-ba5c-4151-8f6a-a3a4bfef1841").then(new Function<Interaction, Object>() {
            @Override
            public Object apply(Interaction interaction) {

              InteractionPlayer player = new InteractionPlayer(interaction, new SpeechPlayStrategy(_speech));
              player.getScriptInterpreter().addBinding(new Ollobot(_service), "ollobot");
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

  private void startService() {
    startService(new Intent(this, BTConnectionService.class));
    bindService(new Intent(this, BTConnectionService.class), _serviceConn, Context.BIND_AUTO_CREATE);
    _serviceBound = true;
  }

  private void stopService() {
    if (_service == null || _service.getBtStatus() != BluetoothManager.STATE_CONNECTED) return;

    _service.finalizeService();
    if (_serviceBound) unbindService(_serviceConn);

    stopService(new Intent(this, BTConnectionService.class));
    _serviceBound = false;
  }

  private void scan() {
    Intent intent = new Intent(this, DeviceListActivity.class);
    startActivityForResult(intent, Constants.REQUEST_CONNECT_DEVICE);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.activity_main, menu);
    return true;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    switch(requestCode) {
      case Constants.REQUEST_CONNECT_DEVICE:
        if (resultCode == Activity.RESULT_OK) {
          String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
          if(address != null && _service != null) _service.connectDevice(address);
        }
        break;

      case Constants.REQUEST_ENABLE_BT:
        if (resultCode == Activity.RESULT_OK) {
          _service.setupBT();
        } else {
          Log.e(TAG, "BT is not enabled");
          Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
        }
        break;
    }

    this._speech.processResult(requestCode, resultCode, data);
  }

  @Override
  public void onDestroy() {
    _ollobot.stop();
    stopService();
    super.onDestroy();
  }
}