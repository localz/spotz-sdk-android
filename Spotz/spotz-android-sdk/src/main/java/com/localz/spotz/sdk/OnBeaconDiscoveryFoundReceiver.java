package com.localz.spotz.sdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.localz.proximity.ble.BleData;
import com.localz.proximity.ble.BleManager;
import com.localz.spotz.api.models.Response;
import com.localz.spotz.api.models.request.v1.SpotzGetRequest;
import com.localz.spotz.api.models.response.v1.SpotzGetResponse;
import com.localz.spotz.sdk.api.GetSpotzTask;
import com.localz.spotz.sdk.listeners.ResponseListenerAdapter;
import com.localz.spotz.sdk.models.Spot;

import java.util.Map;

/**
 * Broadcast receiver that is called as soon as a BLE device is found.
 * Note it is called for every device that is found during the scan duration.
 */
public class OnBeaconDiscoveryFoundReceiver extends BroadcastReceiver {
    private static final String TAG = OnBeaconDiscoveryFoundReceiver.class.getSimpleName();
    private static final String BROADCAST = ".SPOTZ_ON_SPOT_ENTER";
    private static SharedPreferences sharedPreferences;

    @Override
    public void onReceive(final Context context, Intent intent) {
        final BleData bleData = (BleData) intent.getSerializableExtra(BleManager.EXTRA_BLE_DATA);

        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(Spotz.class.getName(), Context.MODE_PRIVATE);
        }

        // App is already initialized, let's rock and roll
        if (Spotz.getInstance().isInitialized()) {
            Map<BleData, String> cachedSpotzIdMap = Spotz.getInstance().getCachedSpotzIdMap();
            final String spotzId = cachedSpotzIdMap.get(bleData);

            if (spotzId == null) {
                // We don't have a cached spot, let's fetch from server
                SpotzGetRequest request = new SpotzGetRequest(bleData.uuid, bleData.major, bleData.minor);
                new GetSpotzTask(new ResponseListenerAdapter<SpotzGetResponse>() {
                    @Override
                    public void onSuccess(Response<SpotzGetResponse> response) {
                        Spotz.getInstance().getCachedSpotzMap().put(response.data._id, response.data);
                        Spotz.getInstance().getCachedSpotzIdMap().put(bleData, response.data._id);
                        inSpot(context, response.data);
                    }
                }).execute(request);
            }
            else {
                // Spot id was found in our cache. Let's check if we have also have the spot cached.
                if (!isAlreadyInSpot(spotzId)) {
                    Map<String, SpotzGetResponse> cachedSpotzMap = Spotz.getInstance().getCachedSpotzMap();
                    SpotzGetResponse spot = cachedSpotzMap.get(spotzId);
                    if (spot == null) {
                        SpotzGetRequest request = new SpotzGetRequest(spotzId);
                        new GetSpotzTask(new ResponseListenerAdapter<SpotzGetResponse>() {
                            @Override
                            public void onSuccess(Response<SpotzGetResponse> response) {
                                Spotz.getInstance().getCachedSpotzMap().put(spotzId, response.data);

                                inSpot(context, response.data);
                            }
                        }).execute(request);
                    }
                    else {
                        inSpot(context, spot);
                    }
                }
            }
        }
        else {
            // TODO for now we do nothing. We don't support when the app has been destroyed.
            Log.w(TAG, "SDK not initialized");
        }
    }

    private boolean isAlreadyInSpot(String spotId) {
        return sharedPreferences.getBoolean(spotId, false);
    }

    private void inSpot(Context context, SpotzGetResponse spotzGetResponse) {
        sharedPreferences.edit().putBoolean(spotzGetResponse._id, true).apply();

        Intent broadcastIntent = new Intent(context.getPackageName() + BROADCAST);
        broadcastIntent.putExtra(Spotz.EXTRA_SPOTZ, Spot.clone(spotzGetResponse));
        context.sendBroadcast(broadcastIntent);
    }

}