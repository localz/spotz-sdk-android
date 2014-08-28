package com.localz.spotz.sdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.localz.proximity.ble.BleData;
import com.localz.proximity.ble.BleManager;
import com.localz.spotz.api.models.ActivityType;
import com.localz.spotz.api.models.Response;
import com.localz.spotz.api.models.request.v1.ActivityReportPostRequest;
import com.localz.spotz.api.models.request.v1.SpotzGetRequest;
import com.localz.spotz.api.models.response.v1.ActivityReportPostResponse;
import com.localz.spotz.api.models.response.v1.SpotzGetResponse;
import com.localz.spotz.sdk.api.ActivityReportTask;
import com.localz.spotz.sdk.api.GetSpotzTask;
import com.localz.spotz.sdk.listeners.ResponseListenerAdapter;
import com.localz.spotz.sdk.models.Spot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OnBeaconDiscoveryFinishedReceiver extends BroadcastReceiver {
    private static final String BROADCAST = ".SPOTZ_ON_SPOT_EXIT";

    private static final String TAG = OnBeaconDiscoveryFinishedReceiver.class.getSimpleName();
    private static Set<String> previousScanVisibleSpotz = new HashSet<String>();
    private static Set<String> previousScanVisibleBeacons = new HashSet<String>();
    private static SharedPreferences sharedPreferences;

    @Override
    public void onReceive(final Context context, Intent intent) {
        List<BleData> bleDatas = (ArrayList<BleData>) intent.getSerializableExtra(BleManager.EXTRA_BLE_DATA);

        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(OnBeaconDiscoveryFoundReceiver.class.getName(), Context.MODE_PRIVATE);
        }

        if (bleDatas != null) {
            // App is already initialized, let's rock and roll
            if (Spotz.getInstance().isInitialized()) {
                sendActivityLogs(context, bleDatas);
            } else {
                // TODO for now we do nothing. We don't support when the app has been destroyed.
                Log.w(TAG, "SDK not initialized");
            }

        }
        // TODO can we use etags to cache spotz data?
    }

    private void sendActivityLogs(final Context context, List<BleData> bleDatas) {
        // Spotz that we have found during this scan
        Set<String> currentScanVisibleSpotz = new HashSet<String>();
        // Beacons that we have found during this scan
        Set<String> currentScanVisibleBeacons = new HashSet<String>();

        final ActivityReportPostRequest activityReportRequest = new ActivityReportPostRequest();

        final Map<BleData, String> cachedSpotzIdMap = Spotz.getInstance().getCachedSpotzIdMap();
        final Map<String, SpotzGetResponse> cachedSpotzMap = Spotz.getInstance().getCachedSpotzIdToSpotzMap();
        for (BleData bleData : bleDatas) {
            final String spotzId = cachedSpotzIdMap.get(bleData);
            if (!TextUtils.isEmpty(spotzId)) {
                currentScanVisibleSpotz.add(spotzId);
            }

            SpotzGetResponse spotzDataForTheBeacon = cachedSpotzMap.get(spotzId);
            if (spotzDataForTheBeacon != null) {
                for (SpotzGetResponse.Beacon b : spotzDataForTheBeacon.beacons) {
                    if (b.uuid.equalsIgnoreCase(bleData.uuid) && b.major == bleData.major && b.minor == bleData.minor
                            && !previousScanVisibleBeacons.contains(b.beaconId)) {
                        // b is the beacon that we see now, get b.beaconId and send it to server's activity log
                        activityReportRequest.addRecord(new Date(), ActivityType.BEACON_ENTER.getName(),
                                b.beaconId, null);

                        currentScanVisibleBeacons.add(b.beaconId);
                        break;
                    }
                }
            }
        }

        previousScanVisibleSpotz.removeAll(currentScanVisibleSpotz);
        previousScanVisibleBeacons.removeAll(currentScanVisibleBeacons);

        // Anything remaining in previousScanVisibleSpotz we have exited!
        for (final String exitedSpotzId : previousScanVisibleSpotz) {
            SpotzGetResponse spot = cachedSpotzMap.get(exitedSpotzId);
            if (spot == null) {
                SpotzGetRequest request = new SpotzGetRequest(exitedSpotzId);
                new GetSpotzTask(new ResponseListenerAdapter<SpotzGetResponse>() {
                    @Override
                    public void onSuccess(Response<SpotzGetResponse> response) {
                        cachedSpotzMap.put(exitedSpotzId, response.data);

                        outSpot(context, response.data, activityReportRequest);

                        // this is special call as it is result of async call, hence can not
                        // use normal activity logging. Create separate request and send it.
                        final ActivityReportPostRequest asyncActivityReportRequest = new ActivityReportPostRequest();
                        asyncActivityReportRequest.addRecord(new Date(), ActivityType.BEACON_EXIT.getName(),
                                null, response.data._id);
                        new ActivityReportTask(new ResponseListenerAdapter<ActivityReportPostResponse>() {
                            @Override
                            public void onSuccess(Response<ActivityReportPostResponse> response) {
                                super.onSuccess(response);
                                // do nothing
                            }
                        }).execute(asyncActivityReportRequest);
                    }
                }).execute(request);
            } else {
                outSpot(context, spot, activityReportRequest);
            }
        }

        // Anything remaining in previousScanVisibleBeacons we have exited!
        for (String exitedBeaconId : previousScanVisibleBeacons) {
            activityReportRequest.addRecord(new Date(), ActivityType.BEACON_EXIT.getName(),
                    exitedBeaconId, null);
        }

        if (activityReportRequest.getLength() > 0) {
            new ActivityReportTask(new ResponseListenerAdapter<ActivityReportPostResponse>() {
                @Override
                public void onSuccess(Response<ActivityReportPostResponse> response) {
                    super.onSuccess(response);
                    // do nothing
                }
            }).execute(activityReportRequest);
        }

        previousScanVisibleSpotz = currentScanVisibleSpotz;
        previousScanVisibleBeacons = currentScanVisibleBeacons;
    }


    private void outSpot(Context context, SpotzGetResponse spotzGetResponse, ActivityReportPostRequest activityReportRequest) {
        sharedPreferences.edit().putBoolean(spotzGetResponse._id, false).apply();

        if (activityReportRequest != null) {
            activityReportRequest.addRecord(new Date(), ActivityType.SPOTZ_EXIT.getName(),
                    null, spotzGetResponse._id);
        }

        Intent broadcastIntent = new Intent(context.getPackageName() + BROADCAST);
        broadcastIntent.putExtra(Spotz.EXTRA_SPOTZ, Spot.clone(spotzGetResponse));
        context.sendBroadcast(broadcastIntent);
    }
}