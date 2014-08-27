package com.localz.spotz.sdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.localz.proximity.ble.BleData;
import com.localz.proximity.ble.BleManager;
import com.localz.spotz.api.models.Response;
import com.localz.spotz.api.models.request.v1.SpotzGetRequest;
import com.localz.spotz.api.models.response.v1.SpotzGetResponse;
import com.localz.spotz.sdk.api.GetSpotzTask;
import com.localz.spotz.sdk.listeners.ResponseListenerAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OnBeaconDiscoveryFinishedReceiver extends BroadcastReceiver {
    private static final String BROADCAST = ".SPOTZ_ON_SPOT_EXIT";

    private static final String TAG = OnBeaconDiscoveryFinishedReceiver.class.getSimpleName();
    private static Set<String> previousScanSpotz = new HashSet<String>();
    private static SharedPreferences sharedPreferences;

    @Override
    public void onReceive(final Context context, Intent intent) {
        List<BleData> bleDatas = (ArrayList<BleData>) intent.getSerializableExtra(BleManager.EXTRA_BLE_DATA);

        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(Spotz.class.getName(), Context.MODE_PRIVATE);
        }

        if (bleDatas != null) {
            // App is already initialized, let's rock and roll
            if (Spotz.getInstance().isInitialized()) {
                Map<BleData, String> cachedSpotzIdMap = Spotz.getInstance().getCachedSpotzIdMap();
                Set<String> currentScanSpotz = new HashSet<String>();
                for (BleData bleData : bleDatas) {
                    final String spotzId = cachedSpotzIdMap.get(bleData);
                    if (!TextUtils.isEmpty(spotzId)) {
                        currentScanSpotz.add(spotzId);
                    }
                }

                previousScanSpotz.removeAll(currentScanSpotz);

                for (final String exitedSpotzId : previousScanSpotz) {
                    Map<String, SpotzGetResponse> cachedSpotzMap = Spotz.getInstance().getCachedSpotzMap();
                    SpotzGetResponse spot = cachedSpotzMap.get(exitedSpotzId);
                    if (spot == null) {
                        SpotzGetRequest request = new SpotzGetRequest(exitedSpotzId);
                        new GetSpotzTask(new ResponseListenerAdapter<SpotzGetResponse>() {
                            @Override
                            public void onSuccess(Response<SpotzGetResponse> response) {
                                Spotz.getInstance().getCachedSpotzMap().put(exitedSpotzId, response.data);

                                outSpot(context, response.data);
                            }
                        }).execute(request);
                    }
                    else {
                        outSpot(context, spot);
                    }
                }

                previousScanSpotz = currentScanSpotz;
            }
            else {
                // TODO for now we do nothing. We don't support when the app has been destroyed.
                Log.w(TAG, "SDK not initialized");
            }

        }

        // TODO can we use etags to cache spotz data?
    }


    private void outSpot(Context context, SpotzGetResponse spotzGetResponse) {
        sharedPreferences.edit().putBoolean(spotzGetResponse._id, false).apply();

        Intent broadcastIntent = new Intent(context.getPackageName() + BROADCAST);
        broadcastIntent.putExtra(Spotz.EXTRA_SPOTZ, spotzGetResponse);
        context.sendBroadcast(broadcastIntent);
    }
}