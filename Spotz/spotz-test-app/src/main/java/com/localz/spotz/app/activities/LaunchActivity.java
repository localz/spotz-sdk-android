package com.localz.spotz.app.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.localz.proximity.ble.BleData;
import com.localz.proximity.ble.BleManager;
import com.localz.spotz.app.R;
import com.localz.spotz.app.widgets.CustomAnimation;
import com.localz.spotz.sdk.Spotz;
import com.localz.spotz.sdk.listeners.InitializationListenerAdapter;

import java.util.ArrayList;

public class LaunchActivity extends Activity {
    public static final String TAG = LaunchActivity.class.getSimpleName();

    private OnBeaconDiscoveredBroadcastReceiver beaconDiscoveredBroadcastReceiver;
    private OnBeaconDiscoveryFinishedBroadcastReceiver beaconDiscoveryFinishedBroadcastReceiver;
    private boolean isInVicinity = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        // Register BLE scan receivers
        beaconDiscoveredBroadcastReceiver = new OnBeaconDiscoveredBroadcastReceiver();
        beaconDiscoveryFinishedBroadcastReceiver = new OnBeaconDiscoveryFinishedBroadcastReceiver();
        registerReceiver(beaconDiscoveredBroadcastReceiver,
                new IntentFilter("com.localz.spotz.app.LOCALZ_BLE_SCAN_FOUND"));
        registerReceiver(beaconDiscoveryFinishedBroadcastReceiver,
                new IntentFilter("com.localz.spotz.app.LOCALZ_BLE_SCAN_FINISH"));

        // Check if device has bluetooth
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // We just show a toast for this test app for informational purposes
            Toast.makeText(this, "Bluetooth not found on your device. You won't be able to use any bluetooth features",
                    Toast.LENGTH_SHORT).show();
        }
        else {
            // For this example app, let's try to ensure bluetooth is switched on
            if (bluetoothAdapter.isEnabled()) {
                initialiseSpotz();
            }
            else {
                showBluetoothNotEnabledDialog();
            }
        }
    }

    private void initialiseSpotz() {
        // Let's initialize the spotz sdk so we can start receiving callbacks for any spotz we find!
        Spotz.getInstance().initialize(this,
                "1234567890123456789012345678901234567890", // Your api key goes here
                "A234567890123456789012345678901234567890", // Your secret key goes here
                new InitializationListenerAdapter() {
                    @Override
                    public void onInitialized() {
                        setNotInVicinity();
                        CustomAnimation.startWaveAnimation(findViewById(R.id.wave));
                    }

                    @Override
                    public void onError(Exception exception) {
                        Log.e(TAG, "Exception while registering device ");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                createErrorDialogInitialising();
                            }
                        });
                    }
                }
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // If this activity is destroyed we want to stop scanning for beacons
        Spotz.getInstance().stopScanningBeacons(this);
        unregisterReceiver(beaconDiscoveredBroadcastReceiver);
        unregisterReceiver(beaconDiscoveryFinishedBroadcastReceiver);
    }

    public class OnBeaconDiscoveredBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            BleData bleData = (BleData) intent.getSerializableExtra(BleManager.EXTRA_BLE_DATA);

            setInVicinity();

            Log.d(TAG, "Discovered " + bleData.uuid + " major:" + bleData.major + " minor:" + bleData.minor);
        }
    }

    public class OnBeaconDiscoveryFinishedBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<BleData> bleDatas = (ArrayList<BleData>) intent.getSerializableExtra(BleManager.EXTRA_BLE_DATA);

            if (bleDatas.size() > 0) {
                for (BleData bleData : bleDatas) {
                    Log.d(TAG, "Discovered " + bleData.uuid + " major:" + bleData.major + " minor:" + bleData.minor);
                }

                setInVicinity();
            }
            else {
                Log.d(TAG, "Discovered no devices");

                setNotInVicinity();
            }
        }
    }

    private void setInVicinity() {
        TextView vicinityText = (TextView) findViewById(R.id.activity_launch_vicinity_text);
        vicinityText.setText(R.string.activity_launch_message_in_vicinity);

        if (!isInVicinity) {
            TransitionDrawable transition = (TransitionDrawable) findViewById(R.id.wave).getBackground();
            transition.resetTransition();
            transition.startTransition(400);
        }

        isInVicinity = true;
    }

    private void setNotInVicinity() {
        TextView vicinityText = (TextView) findViewById(R.id.activity_launch_vicinity_text);
        vicinityText.setText(R.string.activity_launch_message_not_in_vicinity);

        if (isInVicinity) {
            TransitionDrawable transition = (TransitionDrawable) findViewById(R.id.wave).getBackground();
            transition.resetTransition();
            transition.reverseTransition(400);
        }

        isInVicinity = false;
    }

    private void showBluetoothNotEnabledDialog() {
        new AlertDialog.Builder(this).setTitle("Bluetooth not enabled")
                .setMessage("This application requires bluetooth to be enabled.")
                .setCancelable(false)
                .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intentOpenBluetoothSettings = new Intent();
                        intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                        startActivity(intentOpenBluetoothSettings);
                        dialogInterface.dismiss();
                    }
                }).setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        finish();
                    }
                }
        ).show();
    }

    private void createErrorDialogInitialising() {
        new AlertDialog.Builder(LaunchActivity.this).setTitle("Error")
                .setMessage("Unable to initialize application. Please check that you have network connectivity and try again. If the problem persists, contact Localz.")
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        finish();
                    }
                }).show();
    }
}
