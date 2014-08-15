package com.localz.spotz.app.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.localz.proximity.ble.BleData;
import com.localz.proximity.ble.BleManager;
import com.localz.spotz.api.models.Response;
import com.localz.spotz.api.models.response.v1.SpotzGetResponse;
import com.localz.spotz.app.R;
import com.localz.spotz.sdk.Spotz;
import com.localz.spotz.sdk.listeners.InitializationListenerAdapter;
import com.localz.spotz.sdk.listeners.ResponseListenerAdapter;
import com.localz.spotz.sdk.models.InitializedResponse;

import java.util.ArrayList;
import java.util.List;


public class LaunchActivity extends Activity {

    public static final String TAG = "LaunchActivity";
    private BluetoothAdapter mBluetoothAdapter;

    // Need to initialise with Localz Spotz server only once, hence
    // this flag is set to true once it is initialised.
    private boolean isInitialised;

    private BroadcastReceiver noBeaconsFoundMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Discovered noBeaconsFoundMessageReceiver");
            setNotInVicinity();
        }
    };

    private BroadcastReceiver someBeaconsFoundMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Discovered someBeaconsFoundMessageReceiver");
            setInVicinity();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            createDialogNoBluetoothHardware();
        }
    }

    @Override
    protected void onResume() {
        if(checkIfBluetoothEnabled()) {
            initialiseLocalzSpotz();
        }
        super.onResume();
    }

    private boolean checkIfBluetoothEnabled() {
        if (!mBluetoothAdapter.isEnabled()) {
            // Bluetooth not enabled
            createDialogBluetoothNotEnabled();
            return false;
        }

        return true;
    }

    private void initialiseLocalzSpotz() {
        if (!isInitialised) {
            final ProgressBar progressBar = (ProgressBar) findViewById(R.id.activity_launch_progress_bar_id);
            Spotz.getInstance().initialize(this, "1234567890123456789012345678901234567890", "A234567890123456789012345678901234567890", new InitializationListenerAdapter() {
                @Override
                public void onInitialized(InitializedResponse initializedResponse) {
                    super.onInitialized(initializedResponse);
                    Spotz.getInstance().getSpotz(new ResponseListenerAdapter<SpotzGetResponse[]>() {
                        @Override
                        public void onSuccess(Response<SpotzGetResponse[]> response) {
                            progressBar.setVisibility(View.INVISIBLE);
                            LocalBroadcastManager.getInstance(LaunchActivity.this).registerReceiver(noBeaconsFoundMessageReceiver,
                                    new IntentFilter("noBeaconsFound"));

                            LocalBroadcastManager.getInstance(LaunchActivity.this).registerReceiver(someBeaconsFoundMessageReceiver,
                                    new IntentFilter("someBeaconsFound"));
                            List<String> uuids = new ArrayList<String>();
                            if((response.data != null) && (response.data.length > 0)) {
                                for (SpotzGetResponse spotz : response.data) {
                                    uuids.add(spotz.uuid);
                                }
                                Spotz.getInstance().startScanningBeacons(LaunchActivity.this, uuids);
                                // all done, set isInitialised to true, so never try to initialise again
                                isInitialised = true;
                            } else {
                                createErrorDialogNoSpotzRegistered();
                            }
                        }

                        @Override
                        public void onError(Response<SpotzGetResponse[]> response, Exception exception) {
                            super.onError(response, exception);
                            createErrorDialogNoSpotzRegistered();
                        }
                    });
                }

                @Override
                public void onError(Exception exception) {
                    super.onError(exception);
                    Log.e(TAG, "Exception while registering device ");
                    createErrorDialogInitialising();
                }
            });
        }
    }


    private void createDialogBluetoothNotEnabled() {
        new AlertDialog.Builder(this).setTitle("Bluetooth not enabled")
                .setMessage("This application require bluetooth to be enabled.")
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
        }).show();
    }


    private void createErrorDialogInitialising() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    new AlertDialog.Builder(LaunchActivity.this).setTitle("Error")
                            .setMessage("Unable to initialise application. Please check that you have network connectivity and try again. If problem persist, contact Localz.")
                            .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                    finish();
                                }
                            }).show();
                } catch (Exception e) {
                    Log.d(TAG, "Activity probably exited: " + e.getMessage());
                }
            }
        });
    }

    private void createErrorDialogNoSpotzRegistered() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    new AlertDialog.Builder(LaunchActivity.this).setTitle("Error")
                            .setMessage("Unfortunately your application, does not have any spotz registered, please register spotz, before continue.")
                            .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                    finish();
                                }
                            }).show();
                } catch (Exception e) {
                    Log.d(TAG, "Activity probably exited: " + e.getMessage());
                }
            }
        });
    }


    private void createDialogNoBluetoothHardware() {
        new AlertDialog.Builder(this).setTitle("Device does not support Bluetooth")
                .setMessage("Unfortunately, this device does not support bluetooth")
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        finish();
                    }
                }).show();
    }

    @Override
    protected void onDestroy() {
        Spotz.getInstance().stopScanningBeacons(this);
        super.onDestroy();
    }

    private void setInVicinity() {
        TextView vicinityText = (TextView) findViewById(R.id.acitivity_launch_vicinity_text);
        vicinityText.setText(R.string.activity_launch_message_in_vicinity);
    }

    private void setNotInVicinity() {
        TextView vicinityText = (TextView) findViewById(R.id.acitivity_launch_vicinity_text);
        vicinityText.setText(R.string.activity_launch_message_not_in_vicinity);
    }


    public static class OnBeaconDiscoveredBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            BleData bleData = (BleData) intent.getSerializableExtra(BleManager.EXTRA_BLE_DATA);

            Intent intentToBeSent = new Intent("someBeaconsFound");
            LocalBroadcastManager.getInstance(context).sendBroadcast(intentToBeSent);

            Log.d(TAG, "Discovered Beacon !!!!!!!!!!!" + "Scan found " + bleData.uuid + " major:" + bleData.major + " minor:" + bleData.minor);
        }
    }

    public static class OnBeaconDiscoveryFinishedBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<BleData> bleDatas = (ArrayList<BleData>) intent.getSerializableExtra(BleManager.EXTRA_BLE_DATA);


            if (bleDatas.size() > 0) {
                for (BleData bleData : bleDatas) {
                    Log.d(TAG, "Discovered Beacon &&&&&&&&&" + "Scan found " + bleData.uuid + " major:" + bleData.major + " minor:" + bleData.minor);
                }
                Intent intentToBeSent = new Intent("someBeaconsFound");
                LocalBroadcastManager.getInstance(context).sendBroadcast(intentToBeSent);
            } else {
                Intent intentToBeSent = new Intent("noBeaconsFound");
                LocalBroadcastManager.getInstance(context).sendBroadcast(intentToBeSent);
            }
        }
    }
}
