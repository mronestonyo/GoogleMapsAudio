package com.example.googlemapsaudio;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ToggleButton;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, BluetoothListener {
    IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
    IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
    IntentFilter filter3 = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
    //TTS
    private TextToSpeech textToSpeech;
    //AudioManager
    private AudioManager audioManager = null;
    private BluetoothReceiverImpl receiver;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothManager bluetoothManager = null;
    private Set<BluetoothDevice> devices;
    private ToggleButton btnPlayAudio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnPlayAudio = findViewById(R.id.btnPlayAudio);
        btnPlayAudio.setOnClickListener(this);
        setUpBluetoothReceiver();
        setUpTextToSpeech();
    }

    private void setUpBluetoothReceiver() {
        receiver = new BluetoothReceiverImpl();
        receiver.setBluetoothListener(this);
        this.registerReceiver(receiver, filter1);
        this.registerReceiver(receiver, filter2);
        this.registerReceiver(receiver, filter3);
        audioManager = (AudioManager) getApplicationContext().getSystemService(getApplicationContext().AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            bluetoothManager = (BluetoothManager) getApplicationContext().getSystemService(getApplicationContext().BLUETOOTH_SERVICE);
        }
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        devices = bluetoothAdapter.getBondedDevices();
    }

    private void setUpTextToSpeech() {
        textToSpeech = new TextToSpeech(getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.US);
            }
        });
    }

    @Override
    public void onClick(View view) {
        boolean isOn = ((ToggleButton) view).isChecked();
        String TTS_STRING = "“This app demonstrates how audio can be played over a phone’s speaker\n" +
                "even when the phone is connected to Bluetooth";
        if (isOn) {
            if ((BluetoothReceiverImpl.isBluetoothConnected == true) || (devices.size() > 0)) {
                audioManager.setMode(audioManager.MODE_IN_COMMUNICATION);
                audioManager.stopBluetoothSco();
                audioManager.setBluetoothScoOn(true);
                playHardcodedMessage(TTS_STRING);
            } else {
                btnPlayAudio.setChecked(false);
                playHardcodedMessage(TTS_STRING);
            }
        } else {
            audioManager.setMode(audioManager.MODE_NORMAL);
            audioManager.stopBluetoothSco();
            audioManager.setBluetoothScoOn(false);
            audioManager.setSpeakerphoneOn(true);
        }

    }

    private void playHardcodedMessage(String ttsString) {
        //This is to cater the delay during transition from bluetooth speaker to phone; 2000ms
        new Handler().postDelayed(() -> {
            textToSpeech.speak(ttsString, TextToSpeech.QUEUE_FLUSH, null);
        }, 2000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (audioManager != null) {
            audioManager.setMode(AudioManager.MODE_NORMAL);
            audioManager.setSpeakerphoneOn(true);
        }

        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        btnPlayAudio.setChecked(false);
    }

    @Override
    public void clearDevices() {
        for (BluetoothDevice device : devices) {
            try {
                Method m = device.getClass()
                        .getMethod("removeBond", (Class[]) null);
                m.invoke(device, (Object[]) null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
