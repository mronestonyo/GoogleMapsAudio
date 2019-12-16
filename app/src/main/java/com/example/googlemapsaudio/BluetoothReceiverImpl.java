package com.example.googlemapsaudio;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class BluetoothReceiverImpl extends BroadcastReceiver {
    public static Boolean isBluetoothConnected = false;
    private BluetoothListener bluetoothListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                    Log.d("antonhttp", "ACL is now Connected");
                    isBluetoothConnected = true;
                } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                    Log.d("antonhttp", "ACL is now Disconnected");
                    isBluetoothConnected = false;

                    if (bluetoothListener != null)
                        bluetoothListener.clearDevices();
                } else if (action.equals(BluetoothHeadset.STATE_AUDIO_CONNECTED)) {
                    int isConnected = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, BluetoothHeadset.STATE_AUDIO_DISCONNECTED);
                    if (isConnected == BluetoothHeadset.STATE_AUDIO_CONNECTED) {
                        isBluetoothConnected = true;
                    } else if (isConnected == BluetoothHeadset.STATE_AUDIO_DISCONNECTED) {
                        isBluetoothConnected = false;
                    }
                }

                if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                            BluetoothAdapter.ERROR);

                    switch (state) {
                        case BluetoothAdapter.STATE_OFF:
                            //Toast.makeText(context, "Bluetooth off", Toast.LENGTH_SHORT).show();
                            isBluetoothConnected = false;
                            if (bluetoothListener != null)
                                bluetoothListener.clearDevices();
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            //Toast.makeText(context, "Turning Bluetooth off...", Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothAdapter.STATE_ON:
                            isBluetoothConnected = true;
                            //Toast.makeText(context, "Bluetooth on", Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothAdapter.STATE_TURNING_ON:
                            //Toast.makeText(context, "Turning Bluetooth on...", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }
        }
    }

    void setBluetoothListener(BluetoothListener listener) {
        bluetoothListener = listener;
    }
}
