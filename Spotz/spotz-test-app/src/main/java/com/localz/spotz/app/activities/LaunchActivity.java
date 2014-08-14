package com.localz.spotz.app.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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


public class LaunchActivity extends Activity {

    public static final String TAG = "LaunchActivity";

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


        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.activity_launch_progress_bar_id);
        Spotz.getInstance().initialize(this, "1234567890123456789012345678901234567890", "1234567890123456789012345678901234567890", new InitializationListenerAdapter() {
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
                        Spotz.getInstance().startScanningBeacons(LaunchActivity.this, new String[] {response.data[0].uuid});
                    }

                    @Override
                    public void onError(Response<SpotzGetResponse[]> response, Exception exception) {
                        super.onError(response, exception);
                    }
                });
            }

            @Override
            public void onError(Exception exception) {
                super.onError(exception);
                Log.e(TAG, "Exception while registering device ");
            }
        });
    }

    public void setInVicinity() {
        TextView vicinityText = (TextView) findViewById(R.id.acitivity_launch_vicinity_text);
        vicinityText.setText(R.string.activity_launch_message_in_vicinity);
    }

    public void setNotInVicinity() {
        TextView vicinityText = (TextView) findViewById(R.id.acitivity_launch_vicinity_text);
        vicinityText.setText(R.string.activity_launch_message_not_in_vicinity);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.launch, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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
