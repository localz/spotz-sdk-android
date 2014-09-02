package com.localz.spotz.sdk.app;

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
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.localz.spotz.sdk.app.widgets.CustomAnimation;
import com.localz.spotz.sdk.Spotz;
import com.localz.spotz.sdk.listeners.InitializationListenerAdapter;
import com.localz.spotz.sdk.models.Spot;

public class MainActivity extends Activity {
    public static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_BLUETOOTH = 100;

    private OnEnteredSpotBroadcastReceiver enteredSpotBroadcastReceiver;
    private OnExitedSpotReceiver exitedSpotBroadcastReceiver;
    private boolean isInVicinity = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Register BLE scan receivers
        enteredSpotBroadcastReceiver = new OnEnteredSpotBroadcastReceiver();
        exitedSpotBroadcastReceiver = new OnExitedSpotReceiver();
        registerReceiver(enteredSpotBroadcastReceiver,
                new IntentFilter(getPackageName() + ".SPOTZ_ON_SPOT_ENTER"));
        registerReceiver(exitedSpotBroadcastReceiver,
                new IntentFilter(getPackageName() + ".SPOTZ_ON_SPOT_EXIT"));

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
                "your-application-id", // Your application ID goes here
                "your-client-key", // Your client key goes here

                new InitializationListenerAdapter() {
                    @Override
                    public void onInitialized() {
                        // Start scanning for spotz now that we're initialized
                        Spotz.getInstance().startScanningForSpotz(MainActivity.this, Spotz.ScanMode.EAGER);

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
        unregisterReceiver(exitedSpotBroadcastReceiver);
        unregisterReceiver(enteredSpotBroadcastReceiver);
    }

    public class OnEnteredSpotBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Spot spot = (Spot) intent.getSerializableExtra(Spotz.EXTRA_SPOTZ);

            Toast.makeText(context, "Entered the " + spot.name + " spot", Toast.LENGTH_SHORT).show();

            setInVicinity(spot);
        }
    }

    public class OnExitedSpotReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Spot spot = (Spot) intent.getSerializableExtra(Spotz.EXTRA_SPOTZ);

            Toast.makeText(context, "Exited the " + spot.name + " spot", Toast.LENGTH_SHORT).show();

            setNotInVicinity();
        }
    }

    private void setInVicinity(final Spot spot) {
        TextView vicinityText = (TextView) findViewById(R.id.activity_launch_vicinity_text);
        vicinityText.setText(R.string.activity_launch_message_in_vicinity);

        if (!isInVicinity) {
            TransitionDrawable transition = (TransitionDrawable) findViewById(R.id.wave).getBackground();
            transition.resetTransition();
            transition.startTransition(400);

            View metadataButton = findViewById(R.id.activity_launch_metadata_text);
            metadataButton.setVisibility(View.VISIBLE);
            metadataButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, MetadataActivity.class);
                    intent.putExtra(Spotz.EXTRA_SPOTZ, spot);
                    startActivity(intent);
                }
            });
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

            findViewById(R.id.activity_launch_metadata_text).setVisibility(View.INVISIBLE);
        }

        isInVicinity = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_BLUETOOTH) {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            // For this example app, let's try to ensure bluetooth is switched on
            if (bluetoothAdapter.isEnabled()) {
                initialiseSpotz();
            }
            else {
                showBluetoothNotEnabledDialog();
            }
        }
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
                        startActivityForResult(intentOpenBluetoothSettings, REQUEST_BLUETOOTH);
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
        new AlertDialog.Builder(MainActivity.this).setTitle("Error")
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
